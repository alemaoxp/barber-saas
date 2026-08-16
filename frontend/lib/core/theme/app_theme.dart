import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../constants/app_radius.dart';
import '../constants/app_spacing.dart';
import 'app_colors.dart';

abstract final class AppTheme {
  static ThemeData dark() {
    final textTheme = _textTheme();
    final colorScheme = const ColorScheme.dark(
      primary: AppColors.gold,
      secondary: AppColors.blue,
      error: AppColors.error,
      surface: AppColors.card,
      onPrimary: AppColors.background,
      onSecondary: AppColors.textPrimary,
      onError: AppColors.textPrimary,
      onSurface: AppColors.textPrimary,
      outline: AppColors.cardBorder,
      shadow: AppColors.shadow,
      scrim: AppColors.overlay,
    );

    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      scaffoldBackgroundColor: AppColors.background,
      colorScheme: colorScheme,
      textTheme: textTheme,
      cardColor: AppColors.card,
      disabledColor: AppColors.disabled,
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.background,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
        centerTitle: false,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          minimumSize: const Size.fromHeight(AppSpacing.controlHeight),
          backgroundColor: AppColors.gold,
          foregroundColor: AppColors.background,
          disabledBackgroundColor: AppColors.disabled,
          disabledForegroundColor: AppColors.textSecondary,
          elevation: 0,
          shadowColor: AppColors.shadow,
          textStyle: textTheme.labelLarge,
          shape: RoundedRectangleBorder(borderRadius: AppRadius.button),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          minimumSize: const Size.fromHeight(AppSpacing.controlHeight),
          backgroundColor: AppColors.gold,
          foregroundColor: AppColors.background,
          disabledBackgroundColor: AppColors.disabled,
          disabledForegroundColor: AppColors.textSecondary,
          textStyle: textTheme.labelLarge,
          shape: RoundedRectangleBorder(borderRadius: AppRadius.button),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          minimumSize: const Size.fromHeight(AppSpacing.compactControlHeight),
          foregroundColor: AppColors.textSecondary,
          disabledForegroundColor: AppColors.disabled,
          side: const BorderSide(color: AppColors.cardBorder),
          textStyle: textTheme.labelLarge,
          shape: RoundedRectangleBorder(borderRadius: AppRadius.button),
        ),
      ),
      cardTheme: CardThemeData(
        margin: EdgeInsets.zero,
        color: AppColors.card,
        shadowColor: AppColors.shadow,
        surfaceTintColor: Colors.transparent,
        elevation: 0,
        clipBehavior: Clip.antiAlias,
        shape: RoundedRectangleBorder(
          borderRadius: AppRadius.card,
          side: const BorderSide(color: AppColors.cardBorder),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.card,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.md,
          vertical: AppSpacing.md,
        ),
        hintStyle: textTheme.bodyMedium?.copyWith(
          color: AppColors.textSecondary,
        ),
        labelStyle: textTheme.bodyMedium?.copyWith(
          color: AppColors.textSecondary,
        ),
        errorStyle: textTheme.bodySmall?.copyWith(color: AppColors.error),
        prefixIconColor: AppColors.gold,
        suffixIconColor: AppColors.textSecondary,
        enabledBorder: _inputBorder(AppColors.cardBorder),
        focusedBorder: _inputBorder(AppColors.gold),
        errorBorder: _inputBorder(AppColors.error),
        focusedErrorBorder: _inputBorder(AppColors.error),
        disabledBorder: _inputBorder(AppColors.disabled),
      ),
      bottomSheetTheme: BottomSheetThemeData(
        backgroundColor: AppColors.card,
        modalBackgroundColor: AppColors.card,
        surfaceTintColor: Colors.transparent,
        elevation: 0,
        modalElevation: 0,
        shape: RoundedRectangleBorder(borderRadius: AppRadius.sheet),
        clipBehavior: Clip.antiAlias,
        showDragHandle: true,
        dragHandleColor: AppColors.textSecondary,
      ),
      dialogTheme: DialogThemeData(
        backgroundColor: AppColors.card,
        surfaceTintColor: Colors.transparent,
        elevation: 0,
        shadowColor: AppColors.shadow,
        titleTextStyle: textTheme.titleLarge,
        contentTextStyle: textTheme.bodyMedium?.copyWith(
          color: AppColors.textSecondary,
        ),
        shape: RoundedRectangleBorder(borderRadius: AppRadius.dialog),
      ),
      snackBarTheme: SnackBarThemeData(
        backgroundColor: AppColors.card,
        actionTextColor: AppColors.gold,
        disabledActionTextColor: AppColors.disabled,
        contentTextStyle: textTheme.bodyMedium,
        behavior: SnackBarBehavior.floating,
        elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: AppRadius.card),
        insetPadding: const EdgeInsets.all(AppSpacing.md),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: AppColors.card,
        selectedColor: AppColors.gold,
        disabledColor: AppColors.disabled,
        checkmarkColor: AppColors.background,
        side: const BorderSide(color: AppColors.cardBorder),
        shape: RoundedRectangleBorder(borderRadius: AppRadius.chip),
        labelStyle: textTheme.labelMedium,
        secondaryLabelStyle: textTheme.labelMedium?.copyWith(
          color: AppColors.background,
        ),
        padding: const EdgeInsets.symmetric(
          horizontal: AppSpacing.sm,
          vertical: AppSpacing.xs,
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: AppColors.background,
        indicatorColor: AppColors.gold,
        surfaceTintColor: Colors.transparent,
        labelTextStyle: WidgetStateProperty.resolveWith((states) {
          final selected = states.contains(WidgetState.selected);
          return textTheme.labelSmall?.copyWith(
            color: selected ? AppColors.gold : AppColors.textSecondary,
          );
        }),
        iconTheme: WidgetStateProperty.resolveWith((states) {
          final selected = states.contains(WidgetState.selected);
          return IconThemeData(
            color: selected ? AppColors.background : AppColors.textSecondary,
            size: 22,
          );
        }),
      ),
      floatingActionButtonTheme: FloatingActionButtonThemeData(
        backgroundColor: AppColors.blue,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
        focusElevation: 0,
        hoverElevation: 0,
        highlightElevation: 0,
        shape: RoundedRectangleBorder(borderRadius: AppRadius.button),
      ),
      dividerTheme: const DividerThemeData(
        color: AppColors.divider,
        thickness: 1,
      ),
    );
  }

  static OutlineInputBorder _inputBorder(Color color) {
    return OutlineInputBorder(
      borderRadius: AppRadius.input,
      borderSide: BorderSide(color: color),
    );
  }

  static TextTheme _textTheme() {
    return TextTheme(
      displayLarge: GoogleFonts.poppins(
        fontSize: 40,
        fontWeight: FontWeight.w800,
        height: 1.08,
        color: AppColors.textPrimary,
      ),
      displayMedium: GoogleFonts.poppins(
        fontSize: 36,
        fontWeight: FontWeight.w800,
        height: 1.1,
        color: AppColors.textPrimary,
      ),
      displaySmall: GoogleFonts.poppins(
        fontSize: 32,
        fontWeight: FontWeight.w700,
        height: 1.12,
        color: AppColors.textPrimary,
      ),
      headlineLarge: GoogleFonts.poppins(
        fontSize: 30,
        fontWeight: FontWeight.w800,
        height: 1.12,
        color: AppColors.textPrimary,
      ),
      headlineMedium: GoogleFonts.poppins(
        fontSize: 26,
        fontWeight: FontWeight.w800,
        height: 1.12,
        color: AppColors.textPrimary,
      ),
      headlineSmall: GoogleFonts.poppins(
        fontSize: 22,
        fontWeight: FontWeight.w700,
        height: 1.2,
        color: AppColors.textPrimary,
      ),
      titleLarge: GoogleFonts.poppins(
        fontSize: 20,
        fontWeight: FontWeight.w700,
        height: 1.25,
        color: AppColors.textPrimary,
      ),
      titleMedium: GoogleFonts.poppins(
        fontSize: 18,
        fontWeight: FontWeight.w700,
        height: 1.3,
        color: AppColors.textPrimary,
      ),
      titleSmall: GoogleFonts.poppins(
        fontSize: 15,
        fontWeight: FontWeight.w700,
        height: 1.35,
        color: AppColors.textPrimary,
      ),
      bodyLarge: GoogleFonts.poppins(
        fontSize: 16,
        fontWeight: FontWeight.w400,
        height: 1.5,
        color: AppColors.textPrimary,
      ),
      bodyMedium: GoogleFonts.poppins(
        fontSize: 14,
        fontWeight: FontWeight.w400,
        height: 1.45,
        color: AppColors.textPrimary,
      ),
      bodySmall: GoogleFonts.poppins(
        fontSize: 12,
        fontWeight: FontWeight.w400,
        height: 1.4,
        color: AppColors.textSecondary,
      ),
      labelLarge: GoogleFonts.poppins(
        fontSize: 14,
        fontWeight: FontWeight.w700,
        letterSpacing: .2,
        color: AppColors.textPrimary,
      ),
      labelMedium: GoogleFonts.poppins(
        fontSize: 12,
        fontWeight: FontWeight.w600,
        letterSpacing: .4,
        color: AppColors.textSecondary,
      ),
      labelSmall: GoogleFonts.poppins(
        fontSize: 11,
        fontWeight: FontWeight.w500,
        letterSpacing: .4,
        color: AppColors.textSecondary,
      ),
    );
  }
}
