package com.expenseai.manager.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expenseai.manager.domain.model.Expense
import com.expenseai.manager.domain.model.ExpenseCategory
import com.expenseai.manager.domain.model.TransactionType
import com.expenseai.manager.ui.theme.*
import com.expenseai.manager.util.CurrencyUtils
import com.expenseai.manager.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCard(
    expense: Expense,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart && onDelete != null) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = onDelete != null,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        },
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIcon(expense.category, size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (expense.merchant.isNotBlank()) {
                        Text(
                            text = expense.merchant,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = DateUtils.formatDisplay(expense.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val color = when (expense.type) {
                        TransactionType.INCOME -> IncomeGreenLight
                        TransactionType.TRANSFER -> SavingsBlueLight
                        else -> ExpenseRedLight
                    }
                    val prefix = when (expense.type) {
                        TransactionType.INCOME -> "+"
                        TransactionType.TRANSFER -> "→"
                        else -> "-"
                    }
                    Text(
                        text = "$prefix${CurrencyUtils.format(expense.amount, expense.currency)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = expense.currency,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryIcon(category: ExpenseCategory, size: androidx.compose.ui.unit.Dp = 40.dp) {
    val bgColor = getCategoryColor(category).copy(alpha = 0.15f)
    val fgColor = getCategoryColor(category)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.emoji,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

fun getCategoryColor(category: ExpenseCategory): Color = when (category) {
    ExpenseCategory.FOOD_DINING -> CategoryFoodColor
    ExpenseCategory.TRANSPORT -> CategoryTransportColor
    ExpenseCategory.SHOPPING -> CategoryShopping
    ExpenseCategory.HEALTH_FITNESS -> CategoryHealthColor
    ExpenseCategory.ENTERTAINMENT -> CategoryEntertainment
    ExpenseCategory.BILLS_UTILITIES -> CategoryBillsColor
    ExpenseCategory.EDUCATION -> CategoryEducationColor
    ExpenseCategory.TRAVEL -> CategoryTravelColor
    ExpenseCategory.TRANSFER -> CategoryTransferColor
    else -> CategoryOtherColor
}
