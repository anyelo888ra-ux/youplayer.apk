package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import com.example.util.StripePaymentState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StripeCheckoutSheet(
    paymentState: StripePaymentState,
    onDismiss: () -> Unit,
    onProcessPayment: (amount: String, tier: String) -> Unit,
    onResetState: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTier by remember { mutableStateOf("Supporter Pass") }
    var selectedAmount by remember { mutableStateOf("$4.99") }

    ModalBottomSheet(
        onDismissRequest = {
            onResetState()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header with Stripe Logo badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF635BFF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("stripe", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Secure In-App Checkout",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = OnlineGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("256-bit", color = OnlineGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (paymentState) {
                is StripePaymentState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = OnlineGreen,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Payment Succeeded!",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Charge ID: ${paymentState.chargeId}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Thank you for supporting YouPlayer! VIP Supporter Perks Activated.",
                            color = AccentGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                onResetState()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                        ) {
                            Text("Done")
                        }
                    }
                }

                else -> {
                    // Tier selector row
                    Text(
                        text = "Select Support Tier",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SupportTierChip(
                            name = "Tip Creator",
                            amount = "$2.99",
                            isSelected = selectedAmount == "$2.99",
                            onClick = {
                                selectedTier = "Tip Creator"
                                selectedAmount = "$2.99"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SupportTierChip(
                            name = "Supporter",
                            amount = "$4.99",
                            isSelected = selectedAmount == "$4.99",
                            onClick = {
                                selectedTier = "Supporter Pass"
                                selectedAmount = "$4.99"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SupportTierChip(
                            name = "Patron",
                            amount = "$9.99",
                            isSelected = selectedAmount == "$9.99",
                            onClick = {
                                selectedTier = "Patron Pass"
                                selectedAmount = "$9.99"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mock Stripe Card Form
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Card Details (Test Sandbox)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "•••• •••• •••• 4242   •   12/28   •   CVC 982",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Pay Button
                    val isProcessing = paymentState is StripePaymentState.Processing
                    Button(
                        onClick = {
                            if (!isProcessing) {
                                onProcessPayment(selectedAmount, selectedTier)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("stripe_pay_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Processing with Stripe…", color = Color.White)
                        } else {
                            Text(
                                text = "Pay $selectedAmount with Stripe",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupportTierChip(
    name: String,
    amount: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AccentGold.copy(alpha = 0.2f) else DarkSurfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AccentGold) else null,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = amount,
                color = if (isSelected) AccentGold else TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = name,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
