package com.dashlabs.invoicemanagement

import com.dashlabs.invoicemanagement.databaseconnection.Database
import com.dashlabs.invoicemanagement.databaseconnection.InvoiceTable
import com.dashlabs.invoicemanagement.databaseconnection.ProductsTable
import com.dashlabs.invoicemanagement.databaseconnection.twoDecimalFormatted
import com.itextpdf.text.*
import com.itextpdf.text.pdf.FontSelector
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object InvoiceGenerator {

    private val df2 = DecimalFormat("#.##")


    fun makePDF(fileName: File, data: InvoiceTable.MeaningfulInvoice, listproducts: MutableList<Triple<ProductsTable, Double, Int>>): Boolean {
        val curDate = Date()
        val format = SimpleDateFormat("dd-MMM-yyy")
        val date = format.format(curDate)
        try {

            val file = FileOutputStream(fileName)
            val document = Document()
            PdfWriter.getInstance(document, file)

            val irdTable = PdfPTable(2)
            irdTable.addCell(getIRDCell("Quotation No"))
            irdTable.addCell(getIRDCell("Quotation Date"))
            irdTable.addCell(getIRDCell(data.invoiceId)) // pass invoice number
            irdTable.addCell(getIRDCell(date)) // pass invoice date

            val irhTable = PdfPTable(3)
            irhTable.widthPercentage = 100F

            irhTable.addCell(getIRHCell("", PdfPCell.ALIGN_RIGHT))
            irhTable.addCell(getIRHCell("", PdfPCell.ALIGN_RIGHT))
            irhTable.addCell(getIRHCell("Quotation", PdfPCell.ALIGN_RIGHT))
            irhTable.addCell(getIRHCell("", PdfPCell.ALIGN_RIGHT))
            irhTable.addCell(getIRHCell("", PdfPCell.ALIGN_RIGHT))

            val invoiceTable = PdfPCell(irdTable)
            invoiceTable.setBorder(0)
            irhTable.addCell(invoiceTable)

            val fs = FontSelector()
            val font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10F, Font.BOLD)
            fs.addFont(font)
            val bill = fs.process("") // customer information
            var customer = Database.getCustomer(data.customerId)
            val name = Paragraph(customer?.customerName)
            name.indentationLeft = 20F
            val contact = Paragraph(customer?.toString())
            contact.setIndentationLeft(20F)

            val billTable = PdfPTable(6)
            billTable.setWidthPercentage(100F)
            billTable.setWidths(floatArrayOf(1f, 2f, 2f, 2f, 1f, 2f))
            billTable.setSpacingBefore(30.0f)
            billTable.addCell(getBillHeaderCell("Index"))
            billTable.addCell(getBillHeaderCell("Item"))
            billTable.addCell(getBillHeaderCell("Unit Price"))
            billTable.addCell(getBillHeaderCell("Qty"))
            billTable.addCell(getBillHeaderCell("Discount"))
            billTable.addCell(getBillHeaderCell("Amount"))

            fun getAmountFor(baseAmount: Double, discount: Double, quantity: Int): Double? {
                var newPrice = baseAmount.times(quantity).minus(discount)
                newPrice = df2.format(newPrice).toDouble()
                return newPrice.twoDecimalFormatted()
            }


            for (i in listproducts.indices) {
                billTable.addCell(getBillRowCell((i + 1).toString()))
                billTable.addCell(getBillRowCell(listproducts[i].first.productName))
                billTable.addCell(getBillRowCell(listproducts[i].first.amount.toString()))
                billTable.addCell(getBillRowCell(listproducts[i].third.toString()))
                billTable.addCell(getBillRowCell(listproducts[i].second.twoDecimalFormatted().toString()))
                billTable.addCell(getBillRowCell(getAmountFor(listproducts[i].first.amount,
                        listproducts[i].second,
                        listproducts[i].third).toString()))
            }
            for (j in 0 until 15 - listproducts.size) {
                billTable.addCell(getBillRowCell(" "))
                billTable.addCell(getBillRowCell(""))
                billTable.addCell(getBillRowCell(""))
                billTable.addCell(getBillRowCell(""))
                billTable.addCell(getBillRowCell(""))
                billTable.addCell(getBillRowCell(""))
            }

            val validity = PdfPTable(1)
            validity.widthPercentage = 100F
            validity.addCell(getValidityCell(" "))
            validity.addCell(getValidityCell(""))
            validity.addCell(getValidityCell(""))
            validity.addCell(getValidityCell(""))
            val summaryL = PdfPCell(validity)
            summaryL.colspan = 3
            summaryL.setPadding(1.0f)
            billTable.addCell(summaryL)

            val accounts = PdfPTable(2)
            accounts.widthPercentage = 100F
            accounts.addCell(getAccountsCell("Subtotal"))
            accounts.addCell(getAccountsCellR(data.amountTotal.twoDecimalFormatted().toString()))

            accounts.addCell(getAccountsCell("Outstanding"))
            accounts.addCell(getAccountsCellR(data.outstandingAmount.twoDecimalFormatted().toString()))

            val summaryR = PdfPCell(accounts)
            summaryR.colspan = 3
            billTable.addCell(summaryR)

            val describer = PdfPTable(1)
            describer.widthPercentage = 100F
            describer.addCell(getdescCell(" "))
            describer.addCell(getdescCell(" "))

            document.open()//PDF document opened........

            document.add(irhTable)
            document.add(bill)
            document.add(name)
            document.add(contact)
            document.add(billTable)
            document.add(describer)

            document.close()

            file.close()

            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }


    }


    fun getIRHCell(text: String, alignment: Int): PdfPCell {
        val fs = FontSelector()
        val font = FontFactory.getFont(FontFactory.HELVETICA, 14F)
        /*	font.setColor(BaseColor.GRAY);*/
        fs.addFont(font)
        val phrase = fs.process(text)
        val cell = PdfPCell(phrase)
        cell.setPadding(5F)
        cell.setHorizontalAlignment(alignment)
        cell.setBorder(PdfPCell.NO_BORDER)
        return cell
    }

    fun getIRDCell(text: String): PdfPCell {
        val cell = PdfPCell(Paragraph(text))
        cell.setHorizontalAlignment(Element.ALIGN_CENTER)
        cell.setPadding(5.0f)
        cell.setBorderColor(BaseColor.LIGHT_GRAY)
        return cell
    }

    fun getBillHeaderCell(text: String): PdfPCell {
        val fs = FontSelector()
        val font = FontFactory.getFont(FontFactory.HELVETICA, 11F)
        font.setColor(BaseColor.GRAY)
        fs.addFont(font)
        val phrase = fs.process(text)
        val cell = PdfPCell(phrase)
        cell.setHorizontalAlignment(Element.ALIGN_CENTER)
        cell.setPadding(5.0f)
        return cell
    }

    fun getBillRowCell(text: String): PdfPCell {
        val cell = PdfPCell(Paragraph(text))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.setPadding(5.0f)
        cell.borderWidthBottom = 0F
        cell.borderWidthTop = 0F
        return cell
    }

    fun getBillFooterCell(text: String): PdfPCell {
        val cell = PdfPCell(Paragraph(text))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.setPadding(5.0f)
        cell.borderWidthBottom = 0F
        cell.borderWidthTop = 0F
        return cell
    }

    fun getValidityCell(text: String): PdfPCell {
        val fs = FontSelector()
        val font = FontFactory.getFont(FontFactory.HELVETICA, 10F)
        font.setColor(BaseColor.GRAY)
        fs.addFont(font)
        val phrase = fs.process(text)
        val cell = PdfPCell(phrase)
        cell.setBorder(0)
        return cell
    }

    fun getAccountsCell(text: String): PdfPCell {
        val fs = FontSelector()
        val font = FontFactory.getFont(FontFactory.HELVETICA, 10F)
        fs.addFont(font)
        val phrase = fs.process(text)
        val cell = PdfPCell(phrase)
        cell.setBorderWidthRight(0F)
        cell.setBorderWidthTop(0F)
        cell.setPadding(5.0f)
        return cell
    }

    fun getAccountsCellR(text: String): PdfPCell {
        val fs = FontSelector()
        val font = FontFactory.getFont(FontFactory.HELVETICA, 10F)
        fs.addFont(font)
        val phrase = fs.process(text)
        val cell = PdfPCell(phrase)
        cell.setBorderWidthLeft(0F)
        cell.setBorderWidthTop(0F)
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT)
        cell.setPadding(5.0f)
        cell.setPaddingRight(20.0f)
        return cell
    }

    fun getdescCell(text: String): PdfPCell {
        val fs = FontSelector()
        val font = FontFactory.getFont(FontFactory.HELVETICA, 10F)
        font.setColor(BaseColor.GRAY)
        fs.addFont(font)
        val phrase = fs.process(text)
        val cell = PdfPCell(phrase)
        cell.setHorizontalAlignment(Element.ALIGN_CENTER)
        cell.setBorder(0)
        return cell
    }


}
