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
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.Level
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.resources.ResourceLocation
import kotlin.Pair
import kotlin.jvm.optionals.getOrNull

abstract class AbstractConstructEntity(
    entityType: EntityType<out PathfinderMob>,
    world: Level
) : PathfinderMob(entityType, world) {
    private val components = mutableMapOf<ConstructComponentKey<*>, Any>()

    @Suppress("LeakingThis")
    protected val fakePlayer = if (world.isClientSide)
        null
    else
        ConstructFakePlayer(world as ServerLevel, this)

    protected var command: Pair<ConstructCommand<*>, List<Iota>>? = null
    protected var castVm: CastingVM? = null
    protected var error: Component? = null

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

    private fun getOrCreateExecutor(world: ServerLevel): ConstructCommandExecutor {
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

    fun isPlayerAllowed(player: Player?) = (boundPlayerData == null) || (boundPlayerData?.uuid == player?.uuid)

    fun acceptInstructions(
        instructionSet: List<Iota>,
        player: Player?,
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

    fun setLastError(error: Component) {
        this.error = error
        command = null
        castVm = null
    }

    private fun evaluateInstructions(instructionSet: List<Iota>) {
        val serverWorld = level() as? ServerLevel ?: return

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
        world: ServerLevel
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
        if (!level().isClientSide) {
            getOrCreateExecutor(level() as ServerLevel).tick()
        }
    }

    protected fun <T : Any> registerComponent(key: ConstructComponentKey<T>, value: T) {
        components += key to value
    }

    fun <T : Any> getComponent(key: ConstructComponentKey<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return components[key] as T?
    }

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        if (!player.isShiftKeyDown && player.getItemInHand(hand).`is`(HexItems.SCRYING_LENS)) {
            if (!level().isClientSide) {
                val text = Component.literal("STATUS (not yet fully ported)") //TODO fix text builder component
                /*
                val text = buildText {
                    when {
                        error != null -> color(Color(0xFFA500)) {
                            translatable("hexbound.construct.status.error", error!!)
                        }

                        command != null -> color(Color.GREEN) {
                            translatable(
                                "hexbound.construct.status.executing",
                                command!!.first.display(world as ServerLevel)
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
                */

                player.sendSystemMessage(text)
            }

            return InteractionResult.SUCCESS
        }

        return super.mobInteract(player, hand)
    }

    override fun removeWhenFarAway(distanceToClosestPlayer: Double): Boolean {
        return false // equivalent to cannotDespawn() returning true
    }

    override fun getArmorSlots(): Iterable<ItemStack> {
        return mutableListOf()
    }

    override fun getMainArm(): HumanoidArm {
        return HumanoidArm.RIGHT
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        super.addAdditionalSaveData(nbt)

        if (level() is ServerLevel) {
            command?.let {
                nbt.put("command", encodeCommand(it))
            }

            castVm?.let {
                nbt.put("casting_image", it.image.serializeToNbt())
            }

            boundPlayerData?.let {
                nbt.put("boundPlayer", it.toNbt(level().registryAccess()))
            }

            boundPattern?.let {
                nbt.put("boundPattern", it.serializeToNBT())
            }
        }
    }

    private fun <C : ConstructCommand<*>> encodeCommand(commandPair: Pair<C, List<Iota>>): CompoundTag {
        val (command, callback) = commandPair
        val compound = CompoundTag()

        val type = HexboundData.CONSTRUCT_COMMANDS.registry.get()?.getKey(command.getType())
        compound.putString("type", type?.toString() ?: "")

        val callbackList = ListTag()

        callback.forEach {
            callbackList.add(IotaType.serialize(it))
        }

        compound.put("on_complete", callbackList)

        @Suppress("UNCHECKED_CAST")
        (command.getType().codec as? Codec<C>)?.encodeStart(NbtOps.INSTANCE, command)?.result()?.ifPresent {
            compound.put("data", it)
        }

        return compound
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        super.readAdditionalSaveData(nbt)

        command = null
        val serverWorld = level() as? ServerLevel ?: return

        if (nbt.contains("command")) {
            val commandNbt = nbt.getCompound("command")
            val typeId = ResourceLocation.tryParse(commandNbt.getString("type"))
            val type = typeId?.let { HexboundData.CONSTRUCT_COMMANDS.registry.get()?.get(it) }
            
            if (type != null) {
                val result = type.codec.decode(NbtOps.INSTANCE, commandNbt.get("data"))
                val onComplete = commandNbt.getList("on_complete", Tag.TAG_COMPOUND.toInt()).mapNotNull {
                    (it as? CompoundTag)?.let { tag -> IotaType.deserialize(tag, serverWorld) }
                }

                val newCommand = result.result().map { it.first }.orElse(null)

                if (newCommand != null) {
                    executeCommand(newCommand, onComplete, serverWorld)
                }
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
            boundPlayerData = MemorizedPlayerData.fromNbt(nbt.getCompound("boundPlayer"), serverWorld.registryAccess())
        }

        boundPattern = null
        if (nbt.contains("boundPattern")) {
            boundPattern = HexPattern.fromNBT(nbt.getCompound("boundPattern"))
        }
    }
}
