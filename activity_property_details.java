package com.example.maskan;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.widget.EditText;
import android.widget.RatingBar;
public class activity_property_details extends AppCompatActivity {

    private ImageButton btnBack, btnFavorite;
    private ViewPager2 viewPagerImages;
    private LinearLayout layoutIndicators;
    private TextView tvImageCounter, tvPropertyTitle, tvPropertyPrice, tvPropertyLocation;
    private TextView tvBedrooms, tvBathrooms, tvArea, tvPropertyType, tvOfferType;
    private TextView tvDescription, tvContactName, tvContactPhone;
    private Button btnContactMain;
    private Button btnCall;

    private DatabaseHelper databaseHelper;
    private Property currentProperty;
    private List<String> imagePaths = new ArrayList<>();
    private boolean isFavorite = false;


    private RatingBar ratingBar;
    private TextView tvAverageRating, tvTotalRatings;
    private RecyclerView rvComments;
    private Button btnAddRating, btnShowAllComments;
    private LinearLayout layoutRatingSummary;
    private PropertyRatingAdapter commentAdapter;
    private List<PropertyRating> commentList = new ArrayList<>();
    private int propertyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        initializeViews();
        databaseHelper = DatabaseHelper.getInstance(this);

        // ✅ الحصول على بيانات العقار من الـ Intent
        getPropertyData();

        // ✅ إعداد الواجهة
        setupUI();
        setupClickListeners();

        // ✅ التحقق من حالة المفضلة
        checkFavoriteStatus();

        // تهيئة قاعدة البيانات
        databaseHelper = DatabaseHelper.getInstance(this);

// تهيئة عناصر التقييم
        initRatingViews();

// تحميل بيانات التقييم
        loadRatingData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ إعادة التحقق من حالة المفضلة عند العودة للصفحة
        checkFavoriteStatus();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        layoutIndicators = findViewById(R.id.layoutIndicators);
        tvImageCounter = findViewById(R.id.tvImageCounter);
        tvPropertyTitle = findViewById(R.id.tvPropertyTitle);
        tvPropertyPrice = findViewById(R.id.tvPropertyPrice);
        tvPropertyLocation = findViewById(R.id.tvPropertyLocation);
        tvBedrooms = findViewById(R.id.tvBedrooms);
        tvBathrooms = findViewById(R.id.tvBathrooms);
        tvArea = findViewById(R.id.tvArea);
        tvPropertyType = findViewById(R.id.tvPropertyType);
        tvOfferType = findViewById(R.id.tvOfferType);
        tvDescription = findViewById(R.id.tvDescription);
        tvContactName = findViewById(R.id.tvContactName);
        tvContactPhone = findViewById(R.id.tvContactPhone);
        btnCall = findViewById(R.id.btnCall);

        // ✅ المتغير الجديد:
        btnContactMain = findViewById(R.id.btnContactMain);
    }

    private void getPropertyData() {
        android.util.Log.d("PropertyDebug", "=== GET PROPERTY DATA STARTED ===");

        try {
            // ✅ الحصول على propertyId من الـ Intent
            int propertyId = getIntent().getIntExtra("property_id", -1);
            android.util.Log.d("PropertyDebug", "📨 Received property_id: " + propertyId);

            if (propertyId != -1) {
                // ✅ جلب بيانات العقار من قاعدة البيانات باستخدام الـ ID
                currentProperty = databaseHelper.getPropertyById(propertyId);

                if (currentProperty != null) {
                    android.util.Log.d("PropertyDebug", "✅ Loaded from DB - ID: " + currentProperty.getId() +
                            ", Title: " + currentProperty.getTitle() +
                            ", Phone: " + currentProperty.getContactPhone());
                } else {
                    android.util.Log.e("PropertyDebug", "❌ Property not found in DB with ID: " + propertyId);
                    createPropertyFromIntent();
                }
            } else {
                android.util.Log.w("PropertyDebug", "⚠️ No property_id in Intent, using direct data");
                createPropertyFromIntent();
            }

        } catch (Exception e) {
            android.util.Log.e("PropertyDebug", "💥 Error in getPropertyData: " + e.getMessage());
            createDefaultProperty();
        }
    }

    // ✅ دالة مساعدة لإنشاء العقار من الـ Intent
    private void createPropertyFromIntent() {
        String propertyTitle = getIntent().getStringExtra("property_title");
        String propertyLocation = getIntent().getStringExtra("property_location");
        String propertyPrice = getIntent().getStringExtra("property_price");
        String propertyBedrooms = getIntent().getStringExtra("property_bedrooms");
        String propertyBathrooms = getIntent().getStringExtra("property_bathrooms");
        String propertyType = getIntent().getStringExtra("property_type");

        currentProperty = new Property(
                propertyTitle != null ? propertyTitle : "عقار",
                propertyLocation != null ? propertyLocation : "موقع غير محدد",
                propertyPrice != null ? propertyPrice : "0",
                propertyBedrooms != null ? propertyBedrooms : "0",
                propertyBathrooms != null ? propertyBathrooms : "0",
                propertyType != null ? propertyType : "إيجار"
        );

        // ✅ إذا لم يكن هناك ID، نستخدم ID افتراضي (مشكلة تحتاج حل)
        if (currentProperty.getId() == 0) {
            // هذه مشكلة - نحتاج للحصول على ID حقيقي من قاعدة البيانات
            android.util.Log.w("PropertyDetails", "تحذير: العقار لا يحتوي على ID");
        }
    }

    // ✅ دالة إنشاء عقار افتراضي
    private void createDefaultProperty() {
        currentProperty = new Property("عقار", "موقع غير محدد", "0", "0", "0", "إيجار");
        currentProperty.setDescription("بيانات العقار غير متوفرة حالياً.");
        currentProperty.setContactName("غير متوفر");
        currentProperty.setContactPhone("0000000000");
    }

    private void setupUI() {
        // ✅ تعبئة البيانات في الواجهة
        if (currentProperty != null) {
            tvPropertyTitle.setText(currentProperty.getTitle());
            tvPropertyLocation.setText(currentProperty.getLocation());


            if (currentProperty.getContactName() != null && !currentProperty.getContactName().isEmpty()) {
                tvContactName.setText(currentProperty.getContactName());
            } else {
                tvContactName.setText("غير معروف");
                android.util.Log.e("PropertyDetails", "Contact name is null or empty");
            }

            if (currentProperty.getContactPhone() != null && !currentProperty.getContactPhone().isEmpty()) {
                tvContactPhone.setText(currentProperty.getContactPhone());
            } else {
                tvContactPhone.setText("غير متوفر");
                android.util.Log.e("PropertyDetails", "Contact phone is null or empty");
            }

            // ✅ تحسين عرض السعر
            String priceText = currentProperty.getPrice();
            if (currentProperty.getType() != null && currentProperty.getType().equals("إيجار")) {
                priceText += " ر.س/شهرياً";
            } else {
                priceText += " ر.س";
            }
            tvPropertyPrice.setText(priceText);

            tvBedrooms.setText(currentProperty.getBedrooms() + " غرف");
            tvBathrooms.setText(currentProperty.getBathrooms() + " حمام");
            tvArea.setText("150 م²"); // مؤقتاً
            tvPropertyType.setText("شقة");
            tvOfferType.setText(currentProperty.getType());

            // ✅ تلوين نوع العرض
            if (currentProperty.getType() != null && currentProperty.getType().equals("إيجار")) {
                tvOfferType.setBackgroundResource(R.drawable.tag_background_rent);
                tvOfferType.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                tvOfferType.setBackgroundResource(R.drawable.tag_background_sale);
                tvOfferType.setTextColor(getResources().getColor(android.R.color.white));
            }

            if (currentProperty.getDescription() != null) {
                tvDescription.setText(currentProperty.getDescription());
            } else {
                tvDescription.setText("لا يوجد وصف متاح لهذا العقار.");
            }

            if (currentProperty.getContactName() != null) {
                tvContactName.setText(currentProperty.getContactName());
            } else {
                tvContactName.setText("غير معروف");
            }

            if (currentProperty.getContactPhone() != null) {
                tvContactPhone.setText(currentProperty.getContactPhone());
            } else {
                tvContactPhone.setText("غير متوفر");
            }

            // ✅ إعداد معرض الصور
            setupImageGallery();
        }
    }

    private void setupImageGallery() {
        if (currentProperty.hasImages()) {
            imagePaths = currentProperty.getImagePaths();

            // ✅ إنشاء Adapter للصور
            ImagePagerAdapter adapter = new ImagePagerAdapter(this, imagePaths);
            viewPagerImages.setAdapter(adapter);

            // ✅ إعداد عداد الصور
            updateImageCounter(0);

            // ✅ إعداد مؤشرات الصور
            setupIndicators(imagePaths.size());

            // ✅ مستمع لتغيير الصور
            viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updateImageCounter(position);
                    updateIndicators(position);
                }
            });

        } else {
            // ✅ إذا لم توجد صور، إظهار صورة افتراضية
            tvImageCounter.setVisibility(View.GONE);
            layoutIndicators.setVisibility(View.GONE);
        }
    }

    private void setupIndicators(int count) {
        layoutIndicators.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView indicator = new ImageView(this);
            indicator.setImageResource(R.drawable.indicator_dot_inactive);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(8), dpToPx(8)
            );
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            indicator.setLayoutParams(params);

            layoutIndicators.addView(indicator);
        }

        updateIndicators(0);
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < layoutIndicators.getChildCount(); i++) {
            ImageView indicator = (ImageView) layoutIndicators.getChildAt(i);
            if (i == position) {
                indicator.setImageResource(R.drawable.indicator_dot_active);
            } else {
                indicator.setImageResource(R.drawable.indicator_dot_inactive);
            }
        }
    }

    private void updateImageCounter(int position) {
        if (imagePaths.size() > 0) {
            tvImageCounter.setText((position + 1) + "/" + imagePaths.size());
            tvImageCounter.setVisibility(View.VISIBLE);
        } else {
            tvImageCounter.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // ✅ زر العودة
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // ✅ زر المفضلة
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite();
            }
        });

        // ✅ زر الاتصال الرئيسي
        btnContactMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });

        // ✅ زر الاتصال الصغير
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });

        // ✅ أضف هذه الأزرار الجديدة
        setupActionButtons();
    }

    // ✅ دالة جديدة لإعداد أزرار الإجراءات
    private void setupActionButtons() {
        // زر التقييم في صفحة التفاصيل
        Button btnRateDetails = findViewById(R.id.btnRate);
        if (btnRateDetails != null) {
            btnRateDetails.setOnClickListener(v -> {
                showRatingDialog();
            });
        }

        // زر المشاركة في صفحة التفاصيل
        Button btnShareDetails = findViewById(R.id.btnShare);
        if (btnShareDetails != null) {
            btnShareDetails.setOnClickListener(v -> {
                shareProperty();
            });
        }

        // زر الاتصال في صفحة التفاصيل (إذا كان موجوداً)
        Button btnContactDetails = findViewById(R.id.btnContact);
        if (btnContactDetails != null) {
            btnContactDetails.setOnClickListener(v -> {
                makePhoneCall();
            });
        }
    }

    // ✅ دالة التحقق من حالة المفضلة
    // ✅ دالة التحقق من حالة المفضلة
    private void checkFavoriteStatus() {
        if (currentProperty != null && currentProperty.getId() > 0) {
            try {
                isFavorite = databaseHelper.isPropertyInFavorites(currentProperty.getId());
                android.util.Log.d("Favorites", "حالة المفضلة: " + isFavorite + " للعقار: " + currentProperty.getId() + " - " + currentProperty.getTitle());
                updateFavoriteButton();
            } catch (Exception e) {
                android.util.Log.e("Favorites", "خطأ في التحقق من المفضلة: " + e.getMessage());
                isFavorite = false;
                updateFavoriteButton();
            }
        } else {
            android.util.Log.w("Favorites", "لا يمكن التحقق من المفضلة - العقار لا يحتوي على ID صالح");
            isFavorite = false;
            updateFavoriteButton();
        }
    }

    // ✅ دالة تحديث شكل زر المفضلة
    // ✅ دالة تحديث شكل زر المفضلة
    private void updateFavoriteButton() {
        if (btnFavorite == null) {
            android.util.Log.e("Favorites", "زر المفضلة غير موجود في الواجهة!");
            return;
        }

        if (isFavorite) {
            // قلب أحمر (مفضل)
            btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            btnFavorite.setColorFilter(getResources().getColor(android.R.color.holo_red_light));
            android.util.Log.d("Favorites", "زر المفضلة: ❤️ أحمر (مفضل)");
        } else {
            // قلب رمادي (غير مفضل)
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            btnFavorite.setColorFilter(getResources().getColor(android.R.color.darker_gray));
            android.util.Log.d("Favorites", "زر المفضلة: 🤍 رمادي (غير مفضل)");
        }
    }

    private void toggleFavorite() {
        android.util.Log.d("FavoritesDebug", "=== TOGGLE FAVORITE STARTED ===");

        if (currentProperty == null) {
            android.util.Log.e("FavoritesDebug", "❌ currentProperty is NULL");
            Toast.makeText(this, "خطأ: بيانات العقار غير متوفرة", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentProperty.getId() == 0) {
            android.util.Log.e("FavoritesDebug", "❌ Property ID is 0 - Title: " + currentProperty.getTitle());
            Toast.makeText(this, "خطأ: لا يمكن إضافة عقار بدون معرف", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("FavoritesDebug", "🆔 Property ID: " + currentProperty.getId());
        android.util.Log.d("FavoritesDebug", "📝 Property Title: " + currentProperty.getTitle());
        android.util.Log.d("FavoritesDebug", "❤️ Current Favorite Status: " + isFavorite);

        try {
            if (isFavorite) {
                // إزالة من المفضلات
                android.util.Log.d("FavoritesDebug", "🔄 Attempting to remove from favorites...");
                boolean removed = databaseHelper.removeFromFavorites(currentProperty.getId());
                android.util.Log.d("FavoritesDebug", "✅ Remove result: " + removed);

                if (removed) {
                    isFavorite = false;
                    updateFavoriteButton();
                    Toast.makeText(this, "تمت الإزالة من المفضلة ❤️", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "خطأ في الإزالة من المفضلة", Toast.LENGTH_SHORT).show();
                }
            } else {
                // إضافة إلى المفضلات
                android.util.Log.d("FavoritesDebug", "🔄 Attempting to add to favorites...");
                boolean added = databaseHelper.addToFavorites(currentProperty.getId());
                android.util.Log.d("FavoritesDebug", "✅ Add result: " + added);

                if (added) {
                    isFavorite = true;
                    updateFavoriteButton();
                    Toast.makeText(this, "تمت الإضافة إلى المفضلة ❤️", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "خطأ في الإضافة إلى المفضلة", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            android.util.Log.e("FavoritesDebug", "💥 EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "حدث خطأ غير متوقع", Toast.LENGTH_SHORT).show();
        }

        android.util.Log.d("FavoritesDebug", "=== TOGGLE FAVORITE COMPLETED ===");
    }

    private void makePhoneCall() {
        android.util.Log.d("PhoneCall", "=== MAKE PHONE CALL STARTED ===");

        if (currentProperty == null) {
            android.util.Log.e("PhoneCall", "❌ currentProperty is null");
            Toast.makeText(this, "بيانات العقار غير متوفرة", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = currentProperty.getContactPhone();

        // ✅ تنظيف وتحضير رقم الهاتف
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            android.util.Log.e("PhoneCall", "❌ Phone number is null or empty");
            Toast.makeText(this, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ إزالة المسافات والأحرف غير المرغوبة
        phoneNumber = phoneNumber.trim().replaceAll("\\s+", "").replaceAll("[^0-9+]", "");

        // ✅ التحقق من صحة رقم الهاتف
        if (phoneNumber.isEmpty()) {
            android.util.Log.e("PhoneCall", "❌ Phone number is invalid after cleaning: " + currentProperty.getContactPhone());
            Toast.makeText(this, "رقم الهاتف غير صالح", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ إضافة رمز الدولة إذا لم يكن موجوداً (افتراضي السعودية +966)
        if (!phoneNumber.startsWith("+") && !phoneNumber.startsWith("00")) {
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "+966" + phoneNumber.substring(1);
            } else {
                phoneNumber = "+966" + phoneNumber;
            }
        }

        android.util.Log.d("PhoneCall", "📞 Prepared phone number: " + phoneNumber);

        try {
            // ✅ إنشاء نية الاتصال
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));

            // ✅ التحقق من وجود تطبيق يمكنه التعامل مع الاتصال
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                android.util.Log.d("PhoneCall", "✅ Phone call intent started successfully");

                // ✅ تسجيل الاتصال في قاعدة البيانات
                logPropertyContact(currentProperty.getId());

                Toast.makeText(this, "جاري الاتصال بـ: " + phoneNumber, Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.e("PhoneCall", "❌ No app available to handle phone call");
                Toast.makeText(this, "لا يوجد تطبيق للاتصال على جهازك", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            android.util.Log.e("PhoneCall", "💥 Error making phone call: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "خطأ في الاتصال: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        android.util.Log.d("PhoneCall", "=== MAKE PHONE CALL COMPLETED ===");
    }
/*
    // ✅ دالة عرض dialog التقييم
    private void showRatingDialog() {
        if (currentProperty == null) {
            Toast.makeText(this, "بيانات العقار غير متوفرة", Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("تقييم العقار");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etComment);

        builder.setPositiveButton("تقييم", (dialog, which) -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString();

            if (rating > 0) {
                // ✅ حفظ التقييم في قاعدة البيانات
                boolean success = addPropertyRating(currentProperty.getId(), rating, comment);
                if (success) {
                    Toast.makeText(this, "شكراً لتقييمك!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "خطأ في حفظ التقييم", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "يرجى اختيار تقييم", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }*/

    // ✅ دالة إضافة التقييم إلى قاعدة البيانات
    private boolean addPropertyRating(int propertyId, float rating, String comment) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("property_id", propertyId);
        values.put("rating", rating);
        values.put("comment", comment);
        values.put("created_at", "CURRENT_TIMESTAMP");

        try {
            long result = db.insert("property_ratings", null, values);
            return result != -1;
        } catch (Exception e) {
            android.util.Log.e("PropertyDetails", "Error adding rating: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // ✅ دالة مشاركة العقار
    private void shareProperty() {
        if (currentProperty == null) {
            Toast.makeText(this, "بيانات العقار غير متوفرة", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareText = "🏠 " + currentProperty.getTitle() + "\n\n" +
                "📍 " + currentProperty.getLocation() + "\n" +
                "💰 السعر: " + currentProperty.getPrice() + " ر.س\n" +
                "🛏️ الغرف: " + currentProperty.getBedrooms() + "\n" +
                "🚿 الحمامات: " + currentProperty.getBathrooms() + "\n\n" +
                "📱 عبر تطبيق مسكن";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "عقار: " + currentProperty.getTitle());

        startActivity(Intent.createChooser(shareIntent, "مشاركة العقار"));

        // ✅ تسجيل المشاركة في قاعدة البيانات
        logPropertyShare(currentProperty.getId());
    }

    // ✅ دالة تسجيل المشاركة في قاعدة البيانات
    private void logPropertyShare(int propertyId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("property_id", propertyId);
        values.put("shared_at", "CURRENT_TIMESTAMP");

        try {
            db.insert("property_shares", null, values);
            android.util.Log.d("PropertyDetails", "تم تسجيل مشاركة العقار: " + propertyId);
        } catch (Exception e) {
            android.util.Log.e("PropertyDetails", "Error logging share: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    // ✅ دالة تسجيل الاتصال في قاعدة البيانات
    private void logPropertyContact(int propertyId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("property_id", propertyId);
        values.put("contacted_at", "CURRENT_TIMESTAMP");

        try {
            db.insert("property_contacts", null, values);
            android.util.Log.d("PropertyDetails", "تم تسجيل اتصال بالعقار: " + propertyId);
        } catch (Exception e) {
            android.util.Log.e("PropertyDetails", "Error logging contact: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }




    private void initRatingViews() {
        // البحث عن العناصر
        layoutRatingSummary = findViewById(R.id.layoutRatingSummary);
        ratingBar = findViewById(R.id.ratingBarAverage);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalRatings = findViewById(R.id.tvTotalRatings);
        rvComments = findViewById(R.id.rvComments);
        btnAddRating = findViewById(R.id.btnAddRating);
        btnShowAllComments = findViewById(R.id.btnShowAllComments);

        // إعداد RecyclerView للتعليقات
        commentAdapter = new PropertyRatingAdapter(commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        // زر إضافة تقييم
        btnAddRating.setOnClickListener(v -> showRatingDialog());

        // زر عرض كل التعليقات
        if (btnShowAllComments != null) {
            btnShowAllComments.setOnClickListener(v -> showAllCommentsDialog());
        }
    }

    private void loadRatingData() {
        if (databaseHelper == null || propertyId == 0) return;

        // جلب إحصاءات التقييم
        DatabaseHelper.RatingStats stats = databaseHelper.getRatingStats(propertyId);

        if (stats.hasRatings()) {
            // عرض بيانات التقييم
            tvAverageRating.setText(stats.getFormattedAverage());
            ratingBar.setRating(stats.getAverageRating());
            tvTotalRatings.setText("(" + stats.getTotalRatings() + " تقييم)");

            // جلب آخر التعليقات
            loadRecentComments();
        } else {
            // إذا لم يكن هناك تقييمات
            tvAverageRating.setText("0.0");
            ratingBar.setRating(0);
            tvTotalRatings.setText("(لا توجد تقييمات)");

            // رسالة ترحيبية
            TextView tvNoComments = findViewById(R.id.tvNoComments);
            if (tvNoComments != null) {
                tvNoComments.setVisibility(View.VISIBLE);
                tvNoComments.setText("كن أول من يقيم هذا العقار!");
            }
        }
    }

    private void loadRecentComments() {
        if (databaseHelper == null || propertyId == 0) return;

        commentList.clear();
        List<PropertyRating> comments = databaseHelper.getRecentComments(propertyId, 5);
        commentList.addAll(comments);
        commentAdapter.notifyDataSetChanged();

        // عرض/إخفاء رسالة عدم وجود تعليقات
        TextView tvNoComments = findViewById(R.id.tvNoComments);
        if (tvNoComments != null) {
            tvNoComments.setVisibility(commentList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("تقييم العقار");

        // إنشاء محتوى Dialog برمجياً
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // RatingBar
        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        ratingBar.setRating(3);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        ratingBar.setLayoutParams(params);
        layout.addView(ratingBar);

        // EditText للتعليق
        EditText etComment = new EditText(this);
        etComment.setHint("اكتب تعليقك هنا (اختياري)");
        etComment.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etComment.setMinHeight(200);
        etComment.setGravity(Gravity.TOP | Gravity.START);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        etParams.topMargin = 20;
        etComment.setLayoutParams(etParams);
        layout.addView(etComment);

        builder.setView(layout);

        builder.setPositiveButton("إرسال", (dialog, which) -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString().trim();

            if (rating > 0) {
                saveRating(rating, comment);
            } else {
                Toast.makeText(this, "يرجى اختيار تقييم", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    private void showAllCommentsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("جميع التعليقات (" + commentList.size() + ")");

        // قائمة التعليقات
        if (commentList.isEmpty()) {
            builder.setMessage("لا توجد تعليقات بعد.");
            builder.setPositiveButton("موافق", null);
        } else {
            // عرض قائمة التعليقات
            String[] commentsArray = new String[commentList.size()];
            for (int i = 0; i < commentList.size(); i++) {
                PropertyRating rating = commentList.get(i);
                String date = formatDate(rating.getCreatedAt());
                commentsArray[i] = "⭐ " + rating.getRating() + "/5\n" +
                        rating.getComment() + "\n" +
                        "📅 " + date;
            }

            builder.setItems(commentsArray, null);
        }

        builder.setNegativeButton("إغلاق", null);
        builder.show();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }



    private void saveRating(float rating, String comment) {
        if (databaseHelper == null || propertyId <= 0) {
            Toast.makeText(this, "خطأ: قاعدة البيانات غير متاحة", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d("PropertyDetails", "محاولة حفظ التقييم - العقار: " + propertyId + ", التقييم: " + rating);

            // حفظ التقييم في قاعدة البيانات
            boolean success = databaseHelper.addPropertyRating(propertyId, rating, comment);

            if (success) {
                Toast.makeText(this, "شكراً لتقييمك! ✅", Toast.LENGTH_SHORT).show();
                Log.d("PropertyDetails", "تم حفظ التقييم بنجاح");

                // إعادة تحميل بيانات التقييمات
                loadRatingData();

                // إعادة تحميل التعليقات
                loadRecentComments();

                // تحديث إحصائيات التقييم (إذا كانت الدالة موجودة)
                try {
                    // إذا كان لديك دالة لتحديث الإحصائيات
                        updateRatingStatistics();
                } catch (Exception e) {
                    Log.d("PropertyDetails", "لا توجد دالة updateRatingStatistics");
                }

            } else {
                Toast.makeText(this, "فشل في حفظ التقييم ❌", Toast.LENGTH_SHORT).show();
                Log.e("PropertyDetails", "فشل في حفظ التقييم");
            }

        } catch (Exception e) {
            Log.e("PropertyDetails", "خطأ في حفظ التقييم: " + e.getMessage());
            Toast.makeText(this, "حدث خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void updateRatingStatistics() {
        // فقط أعيد تحميل البيانات
        loadRatingData();
    }




}