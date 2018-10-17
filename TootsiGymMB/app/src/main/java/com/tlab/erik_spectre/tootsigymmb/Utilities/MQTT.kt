package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.annotation.SuppressLint
import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*
import kotlin.concurrent.schedule

@SuppressLint("StaticFieldLeak")
object MQTT {

    lateinit var client: MqttAndroidClient
    lateinit var connectionOptions: MqttConnectOptions

    private val mqttTimer = Timer("mqtt", true)

    var initialized = false

    fun init(context: Context, address: String, port: String, username: String, password: String) {

        client = MqttAndroidClient(context, "tcp://$address:$port", MqttClient.generateClientId())
        client.setCallback(MQTTConnectionCallback)
        connectionOptions = MqttConnectOptions()
        connectionOptions.userName = username
        connectionOptions.isAutomaticReconnect = true
        connectionOptions.password = password.toCharArray()
        initialized = true

        startConnection()
    }

    private object MQTTConnectionCallback: MqttCallbackExtended {
        override fun connectComplete(b: Boolean, s: String) {
            println("Connect complete, $s")
            subscribeToTopic()
            sendData("#status")
        }

        override fun connectionLost(throwable: Throwable) {
            println("Connect lost")
        }

        @Throws(Exception::class)
        override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
            //println("messageArrived, $mqttMessage")
            DataParser.parseMQTTResponseData(mqttMessage.toString())
        }

        override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
            //println("deliveryComplete")
        }
    }

    private fun startConnection()
    {
        try {
            val token = client.connect(connectionOptions)
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    println("onSuccess")
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
            client.subscribe(MQTT_SUBSCRIBE_TOPIC, 0)
            println("Success subscribe")
        } catch (e: MqttException) {
            println("Failed subscribe")
        }

    }

    private fun publishToTopic(message: String) {
        try {
            client.publish(MQTT_PUBLISH_TOPIC, message.toByteArray(), 1, false)
            //println("Published: ${publish.message}")
        } catch (e: java.lang.Exception) {
            println(e.message)
        }
    }

    fun sendData(data: String) {
        checkConnection()
        publishToTopic(data)
    }

    fun checkConnection() {
        if (!client.isConnected) reconnect()
    }

    private fun reconnect() {
        startConnection()
        waitForReconnect()
    }

    private fun waitForReconnect(counter: Int = 0) {
        if (client.isConnected) {
            println ("Reconnected in ${counter*5} ms")
        } else {
            mqttTimer.schedule(5) {
                if (counter < 400) waitForReconnect(counter + 1)
            }
        }

        if (counter >= 400) println("Failed to reconnect")
    }
}