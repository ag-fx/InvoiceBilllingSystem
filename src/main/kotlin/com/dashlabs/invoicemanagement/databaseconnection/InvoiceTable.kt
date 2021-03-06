package com.dashlabs.invoicemanagement.databaseconnection

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.*

@DatabaseTable(tableName = "invoices")
class InvoiceTable {

    @DatabaseField(canBeNull = false)
    var customerId: Long = 0

    @DatabaseField(generatedId = true)
    var invoiceId: Long = 0

    @DatabaseField(canBeNull = false)
    var dateCreated: Long = 0L

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    var productsPurchased: String = ""

    @DatabaseField(canBeNull = false)
    var dateModified: Long = 0L

    @DatabaseField(canBeNull = false)
    var amountTotal: Double = 0.0

    @DatabaseField(canBeNull = false)
    var outstandingAmount: Double = 0.0

    @DatabaseField(defaultValue = "false")
    var deleted: Boolean = false

    override fun toString(): String {
        return "$customerId $invoiceId $productsPurchased $dateCreated $dateModified"
    }

    fun asMeaningfulInvoice(): MeaningfulInvoice? {
        Database.getCustomer(this.customerId)?.let {
            return MeaningfulInvoice(this.invoiceId.toString(),
                    Date(this.dateModified).toString(),
                    it.customerName,
                    this.customerId,
                    this.dateModified,
                    this.outstandingAmount,
                    amountTotal,
                    this.productsPurchased,
                    this.amountTotal.minus(this.outstandingAmount).toString())
        } ?: kotlin.run {
            return null
        }

    }

    class MeaningfulInvoice(var invoiceId: String,
                            var dateCreated: String,
                            var customerName: String,
                            var customerId: Long,
                            var dateModified: Long,
                            var outstandingAmount: Double,
                            var amountTotal: Double,
                            var productsPurchased: String,
                            var paymentReceived: String)
}