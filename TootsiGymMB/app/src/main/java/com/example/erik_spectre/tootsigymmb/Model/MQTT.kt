package com.example.erik_spectre.tootsigymmb.Model

import android.content.Context
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
        connectionOptions = MqttConnectOptions()
        connectionOptions.userName = username
        connectionOptions.password = password.toCharArray()
        println("Im here!")
        startConnection()
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
            client.subscribe("moon", 0)
            println("Success subscribe")
            publishToTopic("Connected")
        } catch (e: MqttException) {
            println("Failed subscribe")
        }

    }

    private fun publishToTopic(message: String) {
        try {
            val publish = client.publish("moon", message.toByteArray(), 0, false)
            println("Published: ${publish.message}")
        } catch (e: MqttException) {
            println(e.message)
        }
    }

    public fun sendData(data: String) {
        publishToTopic(data)
    }
}