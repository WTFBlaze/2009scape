package plugin.ai.general

import core.game.system.SystemLogger
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.game.world.map.Location
import core.tools.RandomFunction
import plugin.ai.AIPBuilder
import plugin.ai.AIPlayer
import plugin.ai.general.scriptrepository.GreenDragonKiller
import plugin.ai.general.scriptrepository.Idler
import plugin.ai.general.scriptrepository.Script

class GeneralBotCreator {
    //org/crandor/net/packet/in/InteractionPacket.java <<< This is a very useful class for learning to code bots
    constructor(loc: Location?, botScript: Script) {
        botScript.bot = AIPBuilder.create(loc)
        GameWorld.Pulser.submit(BotScriptPulse(botScript))
    }

    constructor(botScript: Script, bot: AIPlayer?) {
        botScript.bot = bot
        GameWorld.Pulser.submit(BotScriptPulse(botScript))
    }

    inner class BotScriptPulse(private val botScript: Script) : Pulse(1){
        var ticks = 0
        init {
            botScript.init()
        }
        var randomDelay = 0

        override fun pulse(): Boolean {
            if(randomDelay > 0){
                randomDelay -= 1
                return false
            }
            if (!botScript.bot.pulseManager.hasPulseRunning()) {
                if (ticks++ >= RandomFunction.random(90000,120000)) {
                    AIPlayer.deregister(botScript.bot.uid)
                    ticks = 0
                    GameWorld.Pulser.submit(TransitionPulse(botScript))
                    return true
                }
                val idleRoll = RandomFunction.random(50)
                if(idleRoll == 2 && botScript !is Idler){
                    randomDelay += RandomFunction.random(2,20)
                    return false
                }
                botScript.tick()
            }
            /*if(botScript is GreenDragonKiller){
                if(botScript.bot.skills.lifepoints <= 0 && botScript.bot.){
                    AIPlayer.deregister(botScript.bot.uid)
                    ticks = 0
                    GameWorld.Pulser.submit(TransitionPulse(botScript))
                    return true
                }
            }*/

            return false
        }

        override fun stop() {
            ticks = 13000
            pulse()
        }
    }

    inner class TransitionPulse(val script: Script) : Pulse(RandomFunction.random(60,200)){
        override fun pulse(): Boolean {
            GameWorld.Pulser.submit(BotScriptPulse(script.newInstance()))
            return true
        }
    }
}