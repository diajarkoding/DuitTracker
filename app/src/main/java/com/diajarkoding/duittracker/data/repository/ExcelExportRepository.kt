package com.diajarkoding.duittracker.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.diajarkoding.duittracker.data.model.Transaction
import com.diajarkoding.duittracker.data.model.TransactionType
import com.diajarkoding.duittracker.ui.features.statistics.CategoryData
import com.diajarkoding.duittracker.utils.CategoryUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val indonesianLocale = Locale.Builder()
    .setLanguage("id")
    .setRegion("ID")
    .build()

@Singleton
class ExcelExportRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val currencyFormat = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
        maximumFractionDigits = 0
    }

    suspend fun exportToExcel(
        transactions: List<Transaction>,
        expenseByCategory: List<CategoryData>,
        incomeByCategory: List<CategoryData>,
        totalExpense: Double,
        totalIncome: Double,
        monthName: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val workbook = XSSFWorkbook()
            val styles = createStyles(workbook)

            val incomeTransactions = transactions.filter { it.type == TransactionType.INCOME }
            val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }

            // Create sheets
            createSummarySheet(workbook, styles, monthName, transactions, totalIncome, totalExpense, 
                incomeByCategory, expenseByCategory, incomeTransactions, expenseTransactions)
            createCategoryDetailSheet(workbook, styles, transactions, incomeByCategory, expenseByCategory)
            createDailyDetailSheet(workbook, styles, transactions)
            createIncomeSheet(workbook, styles, incomeTransactions, incomeByCategory, totalIncome)
            createExpenseSheet(workbook, styles, expenseTransactions, expenseByCategory, totalExpense)
            createAllTransactionsSheet(workbook, styles, transactions)

            val fileName = "DuitTracker_Report_${monthName.replace(" ", "_")}.xlsx"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createSummarySheet(
        workbook: XSSFWorkbook,
        styles: Styles,
        monthName: String,
        transactions: List<Transaction>,
        totalIncome: Double,
        totalExpense: Double,
        incomeByCategory: List<CategoryData>,
        expenseByCategory: List<CategoryData>,
        incomeTransactions: List<Transaction>,
        expenseTransactions: List<Transaction>
    ) {
        val sheet = workbook.createSheet("Summary")
        var rowNum = 0

        // Title
        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue("LAPORAN KEUANGAN - $monthName")
            cellStyle = styles.title
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 5))
        rowNum++

        // Financial Overview
        rowNum = createSectionHeader(sheet, rowNum, "RINGKASAN KEUANGAN", styles.sectionHeader, 5)
        
        val balance = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) (balance / totalIncome) * 100 else 0.0

        rowNum = createLabelValueRow(sheet, rowNum, "Total Pemasukan", currencyFormat.format(totalIncome), styles.normal, styles.income)
        rowNum = createLabelValueRow(sheet, rowNum, "Total Pengeluaran", currencyFormat.format(totalExpense), styles.normal, styles.expense)
        rowNum = createLabelValueRow(sheet, rowNum, "Saldo/Balance", currencyFormat.format(balance), 
            styles.normal, if (balance >= 0) styles.income else styles.expense)
        rowNum = createLabelValueRow(sheet, rowNum, "Tingkat Tabungan", String.format("%.1f%%", savingsRate), 
            styles.normal, if (savingsRate >= 0) styles.income else styles.expense)
        rowNum++

        // Transaction Statistics
        rowNum = createSectionHeader(sheet, rowNum, "STATISTIK TRANSAKSI", styles.sectionHeader, 5)
        
        rowNum = createLabelValueRow(sheet, rowNum, "Total Transaksi", "${transactions.size} transaksi", styles.normal, styles.normal)
        rowNum = createLabelValueRow(sheet, rowNum, "Transaksi Pemasukan", "${incomeTransactions.size} transaksi", styles.normal, styles.income)
        rowNum = createLabelValueRow(sheet, rowNum, "Transaksi Pengeluaran", "${expenseTransactions.size} transaksi", styles.normal, styles.expense)
        
        if (incomeTransactions.isNotEmpty()) {
            val avgIncome = totalIncome / incomeTransactions.size
            val maxIncome = incomeTransactions.maxOf { it.amount }
            val minIncome = incomeTransactions.minOf { it.amount }
            rowNum = createLabelValueRow(sheet, rowNum, "Rata-rata Pemasukan", currencyFormat.format(avgIncome), styles.normal, styles.income)
            rowNum = createLabelValueRow(sheet, rowNum, "Pemasukan Tertinggi", currencyFormat.format(maxIncome), styles.normal, styles.income)
            rowNum = createLabelValueRow(sheet, rowNum, "Pemasukan Terendah", currencyFormat.format(minIncome), styles.normal, styles.income)
        }
        
        if (expenseTransactions.isNotEmpty()) {
            val avgExpense = totalExpense / expenseTransactions.size
            val maxExpense = expenseTransactions.maxOf { it.amount }
            val minExpense = expenseTransactions.minOf { it.amount }
            rowNum = createLabelValueRow(sheet, rowNum, "Rata-rata Pengeluaran", currencyFormat.format(avgExpense), styles.normal, styles.expense)
            rowNum = createLabelValueRow(sheet, rowNum, "Pengeluaran Tertinggi", currencyFormat.format(maxExpense), styles.normal, styles.expense)
            rowNum = createLabelValueRow(sheet, rowNum, "Pengeluaran Terendah", currencyFormat.format(minExpense), styles.normal, styles.expense)
        }
        rowNum++

        // Account Source Breakdown
        rowNum = createSectionHeader(sheet, rowNum, "BERDASARKAN SUMBER AKUN", styles.sectionHeader, 5)
        
        val incomeByAccount = incomeTransactions.groupBy { it.accountSource }
        val expenseByAccount = expenseTransactions.groupBy { it.accountSource }
        
        val accountHeaders = sheet.createRow(rowNum++)
        listOf("Sumber Akun", "Pemasukan", "Pengeluaran", "Selisih").forEachIndexed { idx, h ->
            accountHeaders.createCell(idx).apply {
                setCellValue(h)
                cellStyle = styles.header
            }
        }
        
        val allAccounts = (incomeByAccount.keys + expenseByAccount.keys).distinct()
        allAccounts.forEach { account ->
            val row = sheet.createRow(rowNum++)
            val accIncome = incomeByAccount[account]?.sumOf { it.amount } ?: 0.0
            val accExpense = expenseByAccount[account]?.sumOf { it.amount } ?: 0.0
            val accBalance = accIncome - accExpense
            
            row.createCell(0).apply { setCellValue(account.name); cellStyle = styles.normal }
            row.createCell(1).apply { setCellValue(currencyFormat.format(accIncome)); cellStyle = styles.income }
            row.createCell(2).apply { setCellValue(currencyFormat.format(accExpense)); cellStyle = styles.expense }
            row.createCell(3).apply { 
                setCellValue(currencyFormat.format(accBalance))
                cellStyle = if (accBalance >= 0) styles.income else styles.expense 
            }
        }
        rowNum++

        // Daily Summary
        if (transactions.isNotEmpty()) {
            rowNum = createSectionHeader(sheet, rowNum, "RINGKASAN HARIAN", styles.sectionHeader, 5)
            
            val dailyData = transactions.groupBy { 
                String.format("%02d/%02d/%d", 
                    it.transactionDate.dayOfMonth, 
                    it.transactionDate.monthNumber, 
                    it.transactionDate.year)
            }.toSortedMap(reverseOrder())
            
            val dailyHeaders = sheet.createRow(rowNum++)
            listOf("Tanggal", "Pemasukan", "Pengeluaran", "Selisih", "Jml Transaksi").forEachIndexed { idx, h ->
                dailyHeaders.createCell(idx).apply { setCellValue(h); cellStyle = styles.header }
            }
            
            dailyData.forEach { (date, txs) ->
                val row = sheet.createRow(rowNum++)
                val dayIncome = txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val dayExpense = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val dayBalance = dayIncome - dayExpense
                
                row.createCell(0).apply { setCellValue(date); cellStyle = styles.normal }
                row.createCell(1).apply { setCellValue(currencyFormat.format(dayIncome)); cellStyle = styles.income }
                row.createCell(2).apply { setCellValue(currencyFormat.format(dayExpense)); cellStyle = styles.expense }
                row.createCell(3).apply { 
                    setCellValue(currencyFormat.format(dayBalance))
                    cellStyle = if (dayBalance >= 0) styles.income else styles.expense 
                }
                row.createCell(4).apply { setCellValue(txs.size.toDouble()); cellStyle = styles.center }
            }
        }

        // Set column widths
        for (i in 0..5) sheet.setColumnWidth(i, 5000)
    }

    private fun createCategoryDetailSheet(
        workbook: XSSFWorkbook,
        styles: Styles,
        transactions: List<Transaction>,
        incomeByCategory: List<CategoryData>,
        expenseByCategory: List<CategoryData>
    ) {
        val sheet = workbook.createSheet("Category Details")
        var rowNum = 0

        // Title
        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue("DETAIL PER KATEGORI")
            cellStyle = styles.title
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 6))
        rowNum++

        // Income Categories with daily breakdown
        if (incomeByCategory.isNotEmpty()) {
            rowNum = createSectionHeader(sheet, rowNum, "KATEGORI PEMASUKAN", styles.sectionHeader, 6)
            
            incomeByCategory.sortedByDescending { it.amount }.forEach { categoryData ->
                val categoryTransactions = transactions.filter { 
                    it.type == TransactionType.INCOME && it.category == categoryData.category 
                }
                
                // Category header
                val catRow = sheet.createRow(rowNum++)
                catRow.createCell(0).apply {
                    setCellValue("${CategoryUtils.getDisplayName(categoryData.category)} - ${currencyFormat.format(categoryData.amount)} (${String.format("%.1f%%", categoryData.percentage)})")
                    cellStyle = styles.subHeader
                }
                sheet.addMergedRegion(CellRangeAddress(rowNum - 1, rowNum - 1, 0, 6))
                
                // Daily breakdown for this category
                val dailyBreakdown = categoryTransactions.groupBy {
                    String.format("%02d/%02d/%d",
                        it.transactionDate.dayOfMonth,
                        it.transactionDate.monthNumber,
                        it.transactionDate.year)
                }.toSortedMap(reverseOrder())
                
                // Header for transactions
                val headerRow = sheet.createRow(rowNum++)
                listOf("Tanggal", "Waktu", "Catatan", "Deskripsi", "Akun", "Jumlah").forEachIndexed { idx, h ->
                    headerRow.createCell(idx).apply { setCellValue(h); cellStyle = styles.header }
                }
                
                dailyBreakdown.forEach { (_, dayTxs) ->
                    dayTxs.sortedByDescending { it.transactionDate }.forEach { tx ->
                        val row = sheet.createRow(rowNum++)
                        row.createCell(0).apply {
                            setCellValue(String.format("%02d/%02d/%d",
                                tx.transactionDate.dayOfMonth,
                                tx.transactionDate.monthNumber,
                                tx.transactionDate.year))
                            cellStyle = styles.normal
                        }
                        row.createCell(1).apply {
                            setCellValue(String.format("%02d:%02d", tx.transactionDate.hour, tx.transactionDate.minute))
                            cellStyle = styles.normal
                        }
                        row.createCell(2).apply { setCellValue(tx.note); cellStyle = styles.normal }
                        row.createCell(3).apply { setCellValue(tx.description ?: "-"); cellStyle = styles.normal }
                        row.createCell(4).apply { setCellValue(tx.accountSource.name); cellStyle = styles.normal }
                        row.createCell(5).apply { setCellValue(currencyFormat.format(tx.amount)); cellStyle = styles.income }
                    }
                }
                rowNum++
            }
        }

        rowNum++

        // Expense Categories with daily breakdown
        if (expenseByCategory.isNotEmpty()) {
            rowNum = createSectionHeader(sheet, rowNum, "KATEGORI PENGELUARAN", styles.sectionHeader, 6)
            
            expenseByCategory.sortedByDescending { it.amount }.forEach { categoryData ->
                val categoryTransactions = transactions.filter { 
                    it.type == TransactionType.EXPENSE && it.category == categoryData.category 
                }
                
                // Category header
                val catRow = sheet.createRow(rowNum++)
                catRow.createCell(0).apply {
                    setCellValue("${CategoryUtils.getDisplayName(categoryData.category)} - ${currencyFormat.format(categoryData.amount)} (${String.format("%.1f%%", categoryData.percentage)})")
                    cellStyle = styles.subHeader
                }
                sheet.addMergedRegion(CellRangeAddress(rowNum - 1, rowNum - 1, 0, 6))
                
                // Daily breakdown for this category
                val dailyBreakdown = categoryTransactions.groupBy {
                    String.format("%02d/%02d/%d",
                        it.transactionDate.dayOfMonth,
                        it.transactionDate.monthNumber,
                        it.transactionDate.year)
                }.toSortedMap(reverseOrder())
                
                // Header for transactions
                val headerRow = sheet.createRow(rowNum++)
                listOf("Tanggal", "Waktu", "Catatan", "Deskripsi", "Akun", "Jumlah").forEachIndexed { idx, h ->
                    headerRow.createCell(idx).apply { setCellValue(h); cellStyle = styles.header }
                }
                
                dailyBreakdown.forEach { (_, dayTxs) ->
                    dayTxs.sortedByDescending { it.transactionDate }.forEach { tx ->
                        val row = sheet.createRow(rowNum++)
                        row.createCell(0).apply {
                            setCellValue(String.format("%02d/%02d/%d",
                                tx.transactionDate.dayOfMonth,
                                tx.transactionDate.monthNumber,
                                tx.transactionDate.year))
                            cellStyle = styles.normal
                        }
                        row.createCell(1).apply {
                            setCellValue(String.format("%02d:%02d", tx.transactionDate.hour, tx.transactionDate.minute))
                            cellStyle = styles.normal
                        }
                        row.createCell(2).apply { setCellValue(tx.note); cellStyle = styles.normal }
                        row.createCell(3).apply { setCellValue(tx.description ?: "-"); cellStyle = styles.normal }
                        row.createCell(4).apply { setCellValue(tx.accountSource.name); cellStyle = styles.normal }
                        row.createCell(5).apply { setCellValue(currencyFormat.format(tx.amount)); cellStyle = styles.expense }
                    }
                }
                rowNum++
            }
        }

        // Set column widths
        sheet.setColumnWidth(0, 3500)
        sheet.setColumnWidth(1, 2500)
        sheet.setColumnWidth(2, 6000)
        sheet.setColumnWidth(3, 6000)
        sheet.setColumnWidth(4, 3500)
        sheet.setColumnWidth(5, 5000)
        sheet.setColumnWidth(6, 3500)
    }

    private fun createDailyDetailSheet(
        workbook: XSSFWorkbook,
        styles: Styles,
        transactions: List<Transaction>
    ) {
        val sheet = workbook.createSheet("Daily Details")
        var rowNum = 0

        // Title
        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue("DETAIL TRANSAKSI HARIAN")
            cellStyle = styles.title
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 7))
        rowNum++

        if (transactions.isEmpty()) return

        // Group by date
        val dailyData = transactions.groupBy {
            String.format("%02d/%02d/%d",
                it.transactionDate.dayOfMonth,
                it.transactionDate.monthNumber,
                it.transactionDate.year)
        }.toSortedMap(reverseOrder())

        dailyData.forEach { (date, dayTransactions) ->
            val dayIncome = dayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val dayExpense = dayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val dayBalance = dayIncome - dayExpense

            // Date header with summary
            rowNum = createSectionHeader(sheet, rowNum, 
                "$date | Pemasukan: ${currencyFormat.format(dayIncome)} | Pengeluaran: ${currencyFormat.format(dayExpense)} | Saldo: ${currencyFormat.format(dayBalance)}", 
                styles.sectionHeader, 7)

            // Transaction headers
            val headerRow = sheet.createRow(rowNum++)
            listOf("Waktu", "Tipe", "Kategori", "Catatan", "Deskripsi", "Akun", "Jumlah").forEachIndexed { idx, h ->
                headerRow.createCell(idx).apply { setCellValue(h); cellStyle = styles.header }
            }

            // Transactions for this day (sorted by time descending)
            dayTransactions.sortedByDescending { it.transactionDate }.forEach { tx ->
                val row = sheet.createRow(rowNum++)
                val isIncome = tx.type == TransactionType.INCOME
                val amountStyle = if (isIncome) styles.income else styles.expense

                row.createCell(0).apply {
                    setCellValue(String.format("%02d:%02d", tx.transactionDate.hour, tx.transactionDate.minute))
                    cellStyle = styles.normal
                }
                row.createCell(1).apply {
                    setCellValue(if (isIncome) "Pemasukan" else "Pengeluaran")
                    cellStyle = amountStyle
                }
                row.createCell(2).apply {
                    setCellValue(CategoryUtils.getDisplayName(tx.category))
                    cellStyle = styles.normal
                }
                row.createCell(3).apply { setCellValue(tx.note); cellStyle = styles.normal }
                row.createCell(4).apply { setCellValue(tx.description ?: "-"); cellStyle = styles.normal }
                row.createCell(5).apply { setCellValue(tx.accountSource.name); cellStyle = styles.normal }
                row.createCell(6).apply {
                    val amount = if (isIncome) tx.amount else -tx.amount
                    setCellValue(currencyFormat.format(amount))
                    cellStyle = amountStyle
                }
            }

            // Daily subtotal
            val subtotalRow = sheet.createRow(rowNum++)
            subtotalRow.createCell(0).apply { setCellValue("Subtotal $date"); cellStyle = styles.subtotal }
            sheet.addMergedRegion(CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5))
            subtotalRow.createCell(6).apply {
                setCellValue(currencyFormat.format(dayBalance))
                cellStyle = if (dayBalance >= 0) styles.income else styles.expense
            }

            rowNum++ // Empty row between days
        }

        // Set column widths
        sheet.setColumnWidth(0, 2500)
        sheet.setColumnWidth(1, 3500)
        sheet.setColumnWidth(2, 4500)
        sheet.setColumnWidth(3, 6000)
        sheet.setColumnWidth(4, 6000)
        sheet.setColumnWidth(5, 3500)
        sheet.setColumnWidth(6, 5000)
        sheet.setColumnWidth(7, 3500)
    }

    private fun createIncomeSheet(
        workbook: XSSFWorkbook,
        styles: Styles,
        transactions: List<Transaction>,
        categoryData: List<CategoryData>,
        totalIncome: Double
    ) {
        val sheet = workbook.createSheet("Income")
        var rowNum = 0

        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue("LAPORAN PEMASUKAN")
            cellStyle = styles.title
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 5))
        rowNum++

        // Summary
        rowNum = createSectionHeader(sheet, rowNum, "RINGKASAN PEMASUKAN", styles.sectionHeader, 5)
        rowNum = createLabelValueRow(sheet, rowNum, "Total Pemasukan", currencyFormat.format(totalIncome), styles.normal, styles.income)
        rowNum = createLabelValueRow(sheet, rowNum, "Jumlah Transaksi", "${transactions.size} transaksi", styles.normal, styles.normal)
        
        if (transactions.isNotEmpty()) {
            val avgIncome = totalIncome / transactions.size
            rowNum = createLabelValueRow(sheet, rowNum, "Rata-rata per Transaksi", currencyFormat.format(avgIncome), styles.normal, styles.income)
            
            val maxTx = transactions.maxByOrNull { it.amount }
            if (maxTx != null) {
                rowNum = createLabelValueRow(sheet, rowNum, "Pemasukan Tertinggi", 
                    "${currencyFormat.format(maxTx.amount)} (${CategoryUtils.getDisplayName(maxTx.category)})", 
                    styles.normal, styles.income)
            }
        }
        rowNum++

        // Category Breakdown with details
        if (categoryData.isNotEmpty()) {
            rowNum = createSectionHeader(sheet, rowNum, "PEMASUKAN PER KATEGORI", styles.sectionHeader, 5)
            
            val catHeaders = sheet.createRow(rowNum++)
            listOf("Kategori", "Jumlah", "Persentase", "Transaksi", "Rata-rata").forEachIndexed { idx, h ->
                catHeaders.createCell(idx).apply { setCellValue(h); cellStyle = styles.header }
            }
            
            categoryData.sortedByDescending { it.amount }.forEach { data ->
                val row = sheet.createRow(rowNum++)
                val avg = if (data.transactionCount > 0) data.amount / data.transactionCount else 0.0
                
                row.createCell(0).apply { setCellValue(CategoryUtils.getDisplayName(data.category)); cellStyle = styles.normal }
                row.createCell(1).apply { setCellValue(currencyFormat.format(data.amount)); cellStyle = styles.income }
                row.createCell(2).apply { setCellValue(String.format("%.1f%%", data.percentage)); cellStyle = styles.center }
                row.createCell(3).apply { setCellValue(data.transactionCount.toDouble()); cellStyle = styles.center }
                row.createCell(4).apply { setCellValue(currencyFormat.format(avg)); cellStyle = styles.income }
            }
            rowNum++
        }

        // Transaction Details
        rowNum = createSectionHeader(sheet, rowNum, "DETAIL TRANSAKSI PEMASUKAN", styles.sectionHeader, 6)
        rowNum = createTransactionTable(sheet, rowNum, transactions.sortedByDescending { it.transactionDate }, styles, true)

        for (i in 0..6) sheet.setColumnWidth(i, 4500)
    }

    private fun createExpenseSheet(
        workbook: XSSFWorkbook,
        styles: Styles,
        transactions: List<Transaction>,
        categoryData: List<CategoryData>,
        totalExpense: Double
    ) {
        val sheet = workbook.createSheet("Expense")
        var rowNum = 0

        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue("LAPORAN PENGELUARAN")
            cellStyle = styles.title
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 5))
        rowNum++

        // Summary
        rowNum = createSectionHeader(sheet, rowNum, "RINGKASAN PENGELUARAN", styles.sectionHeader, 5)
        rowNum = createLabelValueRow(sheet, rowNum, "Total Pengeluaran", currencyFormat.format(totalExpense), styles.normal, styles.expense)
        rowNum = createLabelValueRow(sheet, rowNum, "Jumlah Transaksi", "${transactions.size} transaksi", styles.normal, styles.normal)
        
        if (transactions.isNotEmpty()) {
            val avgExpense = totalExpense / transactions.size
            rowNum = createLabelValueRow(sheet, rowNum, "Rata-rata per Transaksi", currencyFormat.format(avgExpense), styles.normal, styles.expense)
            
            val maxTx = transactions.maxByOrNull { it.amount }
            if (maxTx != null) {
                rowNum = createLabelValueRow(sheet, rowNum, "Pengeluaran Terbesar", 
                    "${currencyFormat.format(maxTx.amount)} (${CategoryUtils.getDisplayName(maxTx.category)})", 
                    styles.normal, styles.expense)
            }
        }
        rowNum++

        // Category Breakdown
        if (categoryData.isNotEmpty()) {
            rowNum = createSectionHeader(sheet, rowNum, "PENGELUARAN PER KATEGORI", styles.sectionHeader, 5)
            
            val catHeaders = sheet.createRow(rowNum++)
            listOf("Kategori", "Jumlah", "Persentase", "Transaksi", "Rata-rata").forEachIndexed { idx, h ->
                catHeaders.createCell(idx).apply { setCellValue(h); cellStyle = styles.header }
            }
            
            categoryData.sortedByDescending { it.amount }.forEach { data ->
                val row = sheet.createRow(rowNum++)
                val avg = if (data.transactionCount > 0) data.amount / data.transactionCount else 0.0
                
                row.createCell(0).apply { setCellValue(CategoryUtils.getDisplayName(data.category)); cellStyle = styles.normal }
                row.createCell(1).apply { setCellValue(currencyFormat.format(data.amount)); cellStyle = styles.expense }
                row.createCell(2).apply { setCellValue(String.format("%.1f%%", data.percentage)); cellStyle = styles.center }
                row.createCell(3).apply { setCellValue(data.transactionCount.toDouble()); cellStyle = styles.center }
                row.createCell(4).apply { setCellValue(currencyFormat.format(avg)); cellStyle = styles.expense }
            }
            rowNum++

            // Top 5 Biggest Expenses
            if (transactions.size > 1) {
                rowNum = createSectionHeader(sheet, rowNum, "TOP 5 PENGELUARAN TERBESAR", styles.sectionHeader, 5)
                
                val topHeaders = sheet.createRow(rowNum++)
                listOf("No", "Tanggal", "Kategori", "Catatan", "Jumlah").forEachIndexed { idx, h ->
                    topHeaders.createCell(idx).apply { setCellValue(h); cellStyle = styles.header }
                }
                
                transactions.sortedByDescending { it.amount }.take(5).forEachIndexed { index, tx ->
                    val row = sheet.createRow(rowNum++)
                    row.createCell(0).apply { setCellValue((index + 1).toDouble()); cellStyle = styles.center }
                    row.createCell(1).apply {
                        setCellValue(String.format("%02d/%02d/%d",
                            tx.transactionDate.dayOfMonth,
                            tx.transactionDate.monthNumber,
                            tx.transactionDate.year))
                        cellStyle = styles.normal
                    }
                    row.createCell(2).apply { setCellValue(CategoryUtils.getDisplayName(tx.category)); cellStyle = styles.normal }
                    row.createCell(3).apply { setCellValue(tx.note); cellStyle = styles.normal }
                    row.createCell(4).apply { setCellValue(currencyFormat.format(tx.amount)); cellStyle = styles.expense }
                }
                rowNum++
            }
        }

        // Transaction Details
        rowNum = createSectionHeader(sheet, rowNum, "DETAIL TRANSAKSI PENGELUARAN", styles.sectionHeader, 6)
        rowNum = createTransactionTable(sheet, rowNum, transactions.sortedByDescending { it.transactionDate }, styles, false)

        for (i in 0..6) sheet.setColumnWidth(i, 4500)
    }

    private fun createAllTransactionsSheet(
        workbook: XSSFWorkbook,
        styles: Styles,
        transactions: List<Transaction>
    ) {
        val sheet = workbook.createSheet("All Transactions")
        var rowNum = 0

        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue("SEMUA TRANSAKSI")
            cellStyle = styles.title
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 7))
        rowNum++

        val headerRow = sheet.createRow(rowNum++)
        listOf("Tanggal", "Waktu", "Tipe", "Kategori", "Catatan", "Deskripsi", "Akun", "Jumlah").forEachIndexed { idx, h ->
            headerRow.createCell(idx).apply { setCellValue(h); cellStyle = styles.header }
        }

        transactions.sortedByDescending { it.transactionDate }.forEach { tx ->
            val row = sheet.createRow(rowNum++)
            val isIncome = tx.type == TransactionType.INCOME
            val amountStyle = if (isIncome) styles.income else styles.expense

            row.createCell(0).apply {
                setCellValue(String.format("%02d/%02d/%d",
                    tx.transactionDate.dayOfMonth,
                    tx.transactionDate.monthNumber,
                    tx.transactionDate.year))
                cellStyle = styles.normal
            }
            row.createCell(1).apply {
                setCellValue(String.format("%02d:%02d", tx.transactionDate.hour, tx.transactionDate.minute))
                cellStyle = styles.normal
            }
            row.createCell(2).apply { setCellValue(if (isIncome) "Pemasukan" else "Pengeluaran"); cellStyle = amountStyle }
            row.createCell(3).apply { setCellValue(CategoryUtils.getDisplayName(tx.category)); cellStyle = styles.normal }
            row.createCell(4).apply { setCellValue(tx.note); cellStyle = styles.normal }
            row.createCell(5).apply { setCellValue(tx.description ?: "-"); cellStyle = styles.normal }
            row.createCell(6).apply { setCellValue(tx.accountSource.name); cellStyle = styles.normal }
            row.createCell(7).apply {
                val amount = if (isIncome) tx.amount else -tx.amount
                setCellValue(currencyFormat.format(amount))
                cellStyle = amountStyle
            }
        }

        sheet.setColumnWidth(0, 3500)
        sheet.setColumnWidth(1, 2500)
        sheet.setColumnWidth(2, 3500)
        sheet.setColumnWidth(3, 4500)
        sheet.setColumnWidth(4, 6000)
        sheet.setColumnWidth(5, 6000)
        sheet.setColumnWidth(6, 3500)
        sheet.setColumnWidth(7, 5000)
    }

    private fun createTransactionTable(
        sheet: Sheet,
        startRow: Int,
        transactions: List<Transaction>,
        styles: Styles,
        isIncome: Boolean
    ): Int {
        var rowNum = startRow
        
        val headerRow = sheet.createRow(rowNum++)
        listOf("Tanggal", "Waktu", "Kategori", "Catatan", "Deskripsi", "Akun", "Jumlah").forEachIndexed { idx, h ->
            headerRow.createCell(idx).apply { setCellValue(h); cellStyle = styles.header }
        }

        val amountStyle = if (isIncome) styles.income else styles.expense

        transactions.forEach { tx ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).apply {
                setCellValue(String.format("%02d/%02d/%d",
                    tx.transactionDate.dayOfMonth,
                    tx.transactionDate.monthNumber,
                    tx.transactionDate.year))
                cellStyle = styles.normal
            }
            row.createCell(1).apply {
                setCellValue(String.format("%02d:%02d", tx.transactionDate.hour, tx.transactionDate.minute))
                cellStyle = styles.normal
            }
            row.createCell(2).apply { setCellValue(CategoryUtils.getDisplayName(tx.category)); cellStyle = styles.normal }
            row.createCell(3).apply { setCellValue(tx.note); cellStyle = styles.normal }
            row.createCell(4).apply { setCellValue(tx.description ?: "-"); cellStyle = styles.normal }
            row.createCell(5).apply { setCellValue(tx.accountSource.name); cellStyle = styles.normal }
            row.createCell(6).apply { setCellValue(currencyFormat.format(tx.amount)); cellStyle = amountStyle }
        }

        return rowNum
    }

    private fun createSectionHeader(sheet: Sheet, rowNum: Int, title: String, style: XSSFCellStyle, mergeColumns: Int): Int {
        val row = sheet.createRow(rowNum)
        row.createCell(0).apply { setCellValue(title); cellStyle = style }
        sheet.addMergedRegion(CellRangeAddress(rowNum, rowNum, 0, mergeColumns))
        return rowNum + 1
    }

    private fun createLabelValueRow(
        sheet: Sheet, rowNum: Int, label: String, value: String,
        labelStyle: XSSFCellStyle, valueStyle: XSSFCellStyle
    ): Int {
        val row = sheet.createRow(rowNum)
        row.createCell(0).apply { setCellValue(label); cellStyle = labelStyle }
        row.createCell(1).apply { setCellValue(value); cellStyle = valueStyle }
        return rowNum + 1
    }

    private data class Styles(
        val title: XSSFCellStyle,
        val header: XSSFCellStyle,
        val sectionHeader: XSSFCellStyle,
        val subHeader: XSSFCellStyle,
        val subtotal: XSSFCellStyle,
        val normal: XSSFCellStyle,
        val income: XSSFCellStyle,
        val expense: XSSFCellStyle,
        val center: XSSFCellStyle
    )

    private fun createStyles(workbook: XSSFWorkbook): Styles {
        val title = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true; fontHeightInPoints = 16; color = IndexedColors.DARK_BLUE.index }
            setFont(font)
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }

        val header = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true; fontHeightInPoints = 11; color = IndexedColors.WHITE.index }
            setFont(font)
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            setBorder()
        }

        val sectionHeader = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true; fontHeightInPoints = 12; color = IndexedColors.WHITE.index }
            setFont(font)
            fillForegroundColor = IndexedColors.GREY_50_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
        }

        val subHeader = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true; fontHeightInPoints = 11; color = IndexedColors.DARK_BLUE.index }
            setFont(font)
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
        }

        val subtotal = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true; fontHeightInPoints = 10 }
            setFont(font)
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.RIGHT
            verticalAlignment = VerticalAlignment.CENTER
            setBorder()
        }

        val normal = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
            setBorder()
        }

        val income = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { color = IndexedColors.DARK_GREEN.index; bold = true }
            setFont(font)
            alignment = HorizontalAlignment.RIGHT
            verticalAlignment = VerticalAlignment.CENTER
            setBorder()
        }

        val expense = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { color = IndexedColors.DARK_RED.index; bold = true }
            setFont(font)
            alignment = HorizontalAlignment.RIGHT
            verticalAlignment = VerticalAlignment.CENTER
            setBorder()
        }

        val center = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            setBorder()
        }

        return Styles(title, header, sectionHeader, subHeader, subtotal, normal, income, expense, center)
    }

    private fun XSSFCellStyle.setBorder() {
        borderTop = BorderStyle.THIN
        borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN
        borderRight = BorderStyle.THIN
    }

    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
