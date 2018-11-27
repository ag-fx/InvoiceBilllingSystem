package com.dashlabs.invoicemanagement.view.dashboard

import com.dashlabs.invoicemanagement.databaseconnection.Database
import com.dashlabs.invoicemanagement.view.admin.Admin
import com.dashlabs.invoicemanagement.view.products.Product
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class DashboardController : Controller() {

    val statusProperty = SimpleStringProperty("No Loggedin User")
    var status by statusProperty


    val admingSettingsProperty = SimpleStringProperty("Admin Login!")
    var admingSettings by admingSettingsProperty

    val adminLogin = SimpleBooleanProperty(false)
    var isAdminLogin by adminLogin

    fun adminLoggedin(admin: Admin) {
        runLater { status = "Welcome ${admin.username} !" }
        runLater { admingSettings = "Admin Settings" }
        runLater { isAdminLogin = true }
    }

}