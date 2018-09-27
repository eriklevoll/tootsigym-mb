package com.tlab.erik_spectre.tootsigymmb.Model

import android.content.Context
import com.tlab.erik_spectre.tootsigymmb.Utilities.CanvasData
import com.tlab.erik_spectre.tootsigymmb.Utilities.HoldsCanvas
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MQTT(private val context: Context,
           private val address: String,
           private val port: String,
           private val username: String,
           private val password: String) {

    val clientId = MqttClient.generateClientId()

    lateinit var client: MqttAndroidClient
    lateinit var connectionOptions: MqttConnectOptions

    fun init() {
        client = MqttAndroidClient(context, "tcp://$address:$port", clientId)
        client.setCallback(MQTTConnectionCallback)
        connectionOptions = MqttConnectOptions()
        connectionOptions.userName = username
        connectionOptions.password = password.toCharArray()
        println("Im here!")
        startConnection()
    }

    private object Test {
        fun lulz() {

        }
    }

    private object MQTTConnectionCallback: MqttCallbackExtended {
        override fun connectComplete(b: Boolean, s: String) {
            println("Connect complete, $s")
        }

        override fun connectionLost(throwable: Throwable) {
            println("Connect lost")
        }

        @Throws(Exception::class)
        override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
            println("messageArrived, $mqttMessage")
            parseInputData(mqttMessage.toString())
        }

        override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
            println("deliveryComplete")
        }

        private fun parseInputData(data: String) {
            try {
                val vals = data.split(":")[1]
                val holdData = vals.split(",")
                val rc = holdData[0]
                if (rc == "-1") {
                    HoldsCanvas.clear()
                    return
                }
                val r = holdData[1]
                val g = holdData[2]
                val b = holdData[3]

                if (r == "0" && g == "0" && b == "0") {
                    CanvasData.removeHold(rc)
                } else {
                    CanvasData.addHold(rc, "$r,$g,$b")
                }
            } catch (e: Exception) {
                println("Failed to parse response, ${e.message}")
            }

        }
    }

    private fun startConnection()
    {
        try {
            val token = client.connect(connectionOptions)
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    println("onSuccess")
                    subscribeToTopic()
                    }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    println("onFailure")
                }
            }
        } catch (e: MqttException) {
            println("exception: ${e.message}")
        }
    }

    private fun subscribeToTopic() {
        try {
            client.subscribe("moon_resp", 0)
            println("Success subscribe")
            publishToTopic("Connected")
        } catch (e: MqttException) {
            println("Failed subscribe")
        }

    }

    private fun publishToTopic(message: String) {
        try {
            val publish = client.publish("moon", message.toByteArray(), 1, false)
            println("Published: ${publish.message}")
        } catch (e: MqttException) {
            println(e.message)
        }
    }

    public fun sendData(data: String) {
        publishToTopic(data)
    }
}