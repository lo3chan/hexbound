package coffee.cypher.hexbound.feature.construct.entity

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.utils.downcast
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import coffee.cypher.hexbound.feature.construct.casting.env.ConstructCastEnv
import coffee.cypher.hexbound.feature.construct.command.ConstructCommand
import coffee.cypher.hexbound.feature.construct.command.exception.ConstructCommandException
import coffee.cypher.hexbound.feature.construct.command.exception.UnknownConstructCommandException
import coffee.cypher.hexbound.feature.construct.command.execution.ConstructCommandExecutor
import coffee.cypher.hexbound.feature.construct.entity.component.ConstructComponentKey
import coffee.cypher.hexbound.init.Hexbound
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.hexbound.util.MemorizedPlayerData
import com.mojang.serialization.Codec
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.world.World
import org.quiltmc.qkl.library.nbt.set
import org.quiltmc.qkl.library.text.*
import kotlin.Pair
import kotlin.jvm.optionals.getOrNull

abstract class AbstractConstructEntity(
    entityType: EntityType<out PathAwareEntity>,
    world: World
) : PathAwareEntity(entityType, world) {
    private val components = mutableMapOf<ConstructComponentKey<*>, Any>()

    @Suppress("LeakingThis")
    protected val fakePlayer = if (world.isClient)
        null
    else
        ConstructFakePlayer(world as ServerWorld, this)

    protected var command: Pair<ConstructCommand<*>, List<Iota>>? = null
    protected var castVm: CastingVM? = null
    protected var error: Text? = null

    private var instructionSet: List<Iota>? = null
    var boundPlayerData: MemorizedPlayerData? = null
    var boundPattern: HexPattern? = null

    private lateinit var _executor: ConstructCommandExecutor

    private fun getOrCreateHarness(): CastingVM {
        if (castVm == null) {
            castVm = CastingVM(
                CastingImage(),
                ConstructCastEnv(this)
            )
        }

        return castVm!!
    }

    private fun getOrCreateExecutor(world: ServerWorld): ConstructCommandExecutor {
        if (!this::_executor.isInitialized) {
            _executor = ConstructCommandExecutor(this, world, this::onCommandCompleted, this::onCommandError)
        }

        return _executor
    }

    private fun onCommandCompleted() {
        val (_, callback) = command ?: return

        command = null
        evaluateInstructions(callback)
    }

    private fun onCommandError(error: Throwable) {
        val commandException = if (error is ConstructCommandException) {
            error
        } else {
            UnknownConstructCommandException(error)
        }

        setLastError(commandException.errorText)
    }

    fun isPlayerAllowed(player: PlayerEntity?) = (boundPlayerData == null) || (boundPlayerData?.uuid == player?.uuid)

    fun acceptInstructions(
        instructionSet: List<Iota>,
        player: PlayerEntity?,
        isBroadcasting: Boolean,
        pattern: HexPattern?
    ): Boolean {
        if (!isPlayerAllowed(player)) {
            return false
        }

        if (isBroadcasting && boundPattern != null && pattern?.sigsEqual(boundPattern!!) != true) {
            return false
        }

        this.instructionSet = instructionSet
        return true
    }

    fun setLastError(error: Text) {
        this.error = error
        command = null
        castVm = null
    }

    private fun evaluateInstructions(instructionSet: List<Iota>) {
        val serverWorld = world as? ServerWorld ?: return

        error = null

        val view = getOrCreateHarness().queueExecuteAndWrapIotas(instructionSet, serverWorld)

        if (!view.resolutionType.success) {
            getOrCreateExecutor(serverWorld).cancelCommand()
        }

        if (command == null) {
            castVm = null
        }
    }

    fun executeCommand(
        command: ConstructCommand<*>,
        onComplete: List<Iota>,
        world: ServerWorld
    ) {
        this.command = command to onComplete
        getOrCreateExecutor(world).startCommand(command)
    }

    override fun tick() {
        super.tick()
        fakePlayer?.resetToValidState()
        fakePlayer?.setPos(x, y, z)

        if (instructionSet != null) {
            castVm = null
            evaluateInstructions(instructionSet!!)
            instructionSet = null
        }
        if (!world.isClient) {
            getOrCreateExecutor(world as ServerWorld).tick()
        }
    }

    protected fun <T : Any> registerComponent(key: ConstructComponentKey<T>, value: T) {
        components += key to value
    }

    fun <T : Any> getComponent(key: ConstructComponentKey<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return components[key] as T?
    }

    override fun interactMob(player: PlayerEntity, hand: Hand): ActionResult {
        if (!player.isSneaking && player.getStackInHand(hand).isOf(HexItems.SCRYING_LENS)) {
            if (!world.isClient) {
                val text = buildText {
                    when {
                        error != null -> color(Color(0xFFA500)) {
                            translatable("hexbound.construct.status.error", error!!)
                        }

                        command != null -> color(Color.GREEN) {
                            translatable(
                                "hexbound.construct.status.executing",
                                command!!.first.display(world as ServerWorld)
                            )
                        }

                        else -> translatable("hexbound.construct.status.idle")
                    }

                    boundPattern?.let {
                        literal("\n")
                        translatable("hexbound.construct.status.bound_pattern", PatternIota.display(it))
                    }

                    boundPlayerData?.let {
                        literal("\n")
                        translatable("hexbound.construct.status.bound_player", it.displayName)
                    }
                }

                player.sendSystemMessage(text)
            }

            return ActionResult.SUCCESS
        }

        return super.interactMob(player, hand)
    }

    override fun isPersistent(): Boolean {
        return true
    }

    override fun cannotDespawn(): Boolean {
        return true
    }

    override fun getArmorItems(): Iterable<ItemStack> {
        return mutableListOf()
    }

    override fun getMainArm(): Arm {
        return Arm.RIGHT
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)

        if (world is ServerWorld) {
            command?.let {
                nbt["command"] = encodeCommand(it)
            }

            castVm?.let {
                nbt["casting_image"] = it.image.serializeToNbt()
            }

            boundPlayerData?.let {
                nbt["boundPlayer"] = it.toNbt()
            }

            boundPattern?.let {
                nbt["boundPattern"] = it.serializeToNBT()
            }
        }
    }

    private fun <C : ConstructCommand<*>> encodeCommand(commandPair: Pair<C, List<Iota>>): NbtCompound {
        val (command, callback) = commandPair
        val compound = NbtCompound()

        val type = HexboundData.ModRegistries.CONSTRUCT_COMMANDS.getId(command.getType())
        compound["type"] = type.toString()

        val callbackList = NbtList()

        callback.forEach {
            callbackList.add(IotaType.serialize(it))
        }

        compound["on_complete"] = callbackList

        if (HexboundData.ModRegistries.CONSTRUCT_COMMANDS.get(type) != command.getType()) {
            Hexbound.LOGGER.warn("Construct command type for $command was not registered")
            compound["data"] = NbtCompound()
            return compound
        }

        @Suppress("UNCHECKED_CAST")
        compound["data"] =
            (command.getType().codec as Codec<C>).encodeStart(NbtOps.INSTANCE, command).result().get()

        return compound
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)

        command = null
        val serverWorld = world as? ServerWorld ?: return

        if (nbt.contains("command")) {
            //TODO maybe this can go into a command frame class (low priority)
            val commandNbt = nbt.getCompound("command")
            val typeId = Identifier.tryParse(commandNbt.getString("type"))
            val type = HexboundData.ModRegistries.CONSTRUCT_COMMANDS.get(typeId)
            val result = type.codec.decode(NbtOps.INSTANCE, commandNbt.get("data"))
            val onComplete = commandNbt.getList("on_complete", NbtElement.COMPOUND_TYPE.toInt()).map {
                IotaType.deserialize(it.downcast(NbtCompound.TYPE), serverWorld)
            }

            val newCommand = result.result().getOrNull()?.first

            if (newCommand != null) {
                executeCommand(newCommand, onComplete, serverWorld)
            }
        }

        if (nbt.contains("casting_image")) {
            castVm = CastingVM(
                CastingImage.loadFromNbt(nbt.getCompound("casting_image"), serverWorld),
                ConstructCastEnv(this)
            )
        }

        boundPlayerData = null
        if (nbt.contains("boundPlayer")) {
            boundPlayerData = MemorizedPlayerData.fromNbt(nbt.getCompound("boundPlayer"))
        }

        boundPattern = null
        if (nbt.contains("boundPattern")) {
            boundPattern = HexPattern.fromNBT(nbt.getCompound("boundPattern"))
        }
    }
}
