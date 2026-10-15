package com.Nightjar.caliq5.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.AppViewModel
import caliq5.shared.generated.resources.Res
import caliq5.shared.generated.resources.exemption_rules

@Composable
fun InstructionsScreen(viewModel: AppViewModel) {
    CaliqScreen(title = "كيفية الاستخدام", onBack = { viewModel.goBack() }, scrollable = false) {
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 280.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(), top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 65.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(0)) {
                    ExpandableInstructionCard(
                        title = "ضوابط الإعفاء العام والفردي للعام 2024-2025",
                        initiallyExpanded = true
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                "وفقاً لتوجيهات وزارة التربية العراقية، تكون ضوابط الإعفاء للعام الدراسي 2024 / 2025 كالآتي:",
                                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp, lineHeight = 24.sp)
                            )
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Image(
                                    painter = org.jetbrains.compose.resources.painterResource(Res.drawable.exemption_rules),
                                    contentDescription = "ضوابط الإعفاء",
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.FillWidth
                                )
                            }

                            Text(
                                text = "1. الإعفاء الفردي:", 
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                            )
                            Text("• يُعفى الطالب من أداء الامتحان النهائي في المادة التي يحصل فيها على سعي سنوي 90% فأكثر.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))

                            Text(
                                text = "2. الإعفاء العام:", 
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                            )
                            Text("• يُشمل الطالب بالإعفاء العام إذا حصل على معدل عام لا يقل عن 85%.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                            Text("• بشرط عدم حصوله على درجة سعي سنوي أقل من 75% في أي مادة.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))

                            Text(
                                text = "3. جبر الكسور (للسعي السنوي للمادة فقط):", 
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                            )
                            Text("• الطالب الحاصل على درجة سعي بين (74.6%) و (74.9%) تُرفع إلى 75%.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                            Text("• الطالب الحاصل على درجة سعي بين (89.6%) و (89.9%) تُرفع إلى 90%.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                            Text("• الكسور الأخرى لا يتم جبرُها للأعلى وتُنزل كما هي مع الكسر العشري.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Text(
                                text = "⚠️ تنبيه: المعدل العام للإعفاء (85%) لا يخضع لقاعدة جبر الكسور.", 
                                color = MaterialTheme.colorScheme.error, 
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(12.dp)
                            )
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(1)) {
                    ExpandableInstructionCard(title = "طريقة استخدام \"درجاتي (السجل الشامل)\"") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("تتيح لك هذه الميزة بناء سجل درجات كامل لك أو لطلابك.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                            Text("1. اضغط على \"إضافة سجل صف / طالب\" وأدخل الاسم والصف.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                            Text("2. اختر طريقة الإدخال (فصلي جاهز أو شهريات) لكل فصل دراسي.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                            Text("3. يمكنك إدخال الامتحان النهائي، وإذا كنت معفياً سيقوم التطبيق تلقائياً باعتبار الدرجة النهائية هي درجة السعي.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                            Text("4. يمكنك تصدير درجات الطالب كملف PDF مرتب عبر أيقونة الـ PDF بجانب اسمه، وحفظه مباشرة في جهازك.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(2)) {
                    ExpandableInstructionCard(title = "طريقة استخدام \"حساب الإعفاء\"") {
                        Text("1. اختر صفك الدراسي من القائمة.\n2. أدخل درجة السعي السنوي لكل مادة.\n3. اضغط \"لا تشملني\" للمواد غير المطلوبة.\n4. استخدم \"+ إضافة مادة\" للمواد الأخرى.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                    }
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(3)) {
                    ExpandableInstructionCard(title = "طريقة استخدام \"السعي السنوي\"") {
                        Text("أدخل اسم المادة ودرجات الفصل الأول، نصف السنة، والفصل الثاني للمادة، ثم اضغط \"حساب السعي\".", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                    }
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(4)) {
                    ExpandableInstructionCard(title = "طريقة استخدام \"المعدل الفصلي\"") {
                        Text("أدخل درجات امتحانات الأشهر لهذا الفصل (شهر أول، شهر ثاني، ...)، ثم اضغط حساب.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                    }
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(5)) {
                    ExpandableInstructionCard(title = "طريقة استخدام \"درجة الفصل 2\"") {
                        Text("استخدم هذه الميزة لمعرفة الدرجة التي تحتاجها في الفصل الثاني لتحقيق سعي 75% أو 90%.", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp))
                    }
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(6)) {
                    ExpandableInstructionCard(title = "🔹 إخلاء مسؤولية") {
                        Text(
                            "هذا التطبيق أداة تعليمية غير رسمية تستند إلى التعليمات الرسمية لوزارة التربية العراقية للتسهيل على الطلاب.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp)
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun ExpandableInstructionCard(
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    val arrowAngle by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "instr_arrow_angle"
    )

    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = arrowAngle
                    },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = androidx.compose.animation.core.tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(200)),
                exit = shrinkVertically(
                    animationSpec = androidx.compose.animation.core.tween(200, easing = androidx.compose.animation.core.FastOutLinearInEasing),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
            ) {
                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                        content()
                    }
                }
            }
        }
    }
}
