package ind.syu.kubeIsp

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import java.util.*
import kotlin.collections.ArrayList
import java.util.concurrent.TimeUnit



data class Event(val msg:String,val time:Date=Date())

interface Listener{
    fun on(e:Event)
    fun stop()
}

class EventSource{
    val eventList = ArrayList<Listener>()

    fun addListener(l:Listener){
        eventList.add(l)
    }

    fun addEvent(e: Event){
        eventList.forEach { it.on(e) }
    }

    fun stop(){
        eventList.forEach { it.stop() }
    }
}


class Testclass{

    @Test
    fun testCreate(){
        var source = EventSource()
        Flux.create<Event> { sink->
            source.addListener( object :Listener{
                override fun on(e: Event) {
                    sink.next(e)
                }
                override fun stop() {
                   sink.complete()
                }
            })
        }.subscribe(::println)

        for( i in 0..20){
            val random = Random()
            TimeUnit.MILLISECONDS.sleep(random.nextInt(1000).toLong())

            source.addEvent(Event(i.toString()))
        }
        source.stop()
    }
}