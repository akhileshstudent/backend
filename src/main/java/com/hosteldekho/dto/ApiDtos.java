package com.hosteldekho.dto;

import com.hosteldekho.entity.Enums.*;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class ApiDtos {
    private ApiDtos() {}

    public record AuthRequest(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(max=72) String password) {}
    public record RegisterRequest(@NotBlank @Size(max=120) String name, @NotBlank @Email @Size(max=254) String email,
                                  @NotBlank @Size(min=6, max=72) String password,
                                  @NotBlank @Pattern(regexp="^[0-9+\\- ]{7,20}$") String phone) {}
    public record ManagerSummary(Long id, String name, String email) {}
    public record AuthResponse(String token, ManagerSummary manager) {}
    public record CollegeDto(Long id, String name, String locality, String city, Double latitude, Double longitude, String slug) {}
    public record CollegePageDto(Long id, String name, String slug, String locality, String city, Double latitude, Double longitude, long hostelCount, long vacantBeds, BigDecimal minPricePerMonth, BigDecimal avgPricePerMonth) {}
    public record CollegeLinkDto(@NotNull Long collegeId, @NotNull @DecimalMin("0.0") @DecimalMax("100000.0") Double distanceKm) {}

    public record HostelRequest(
        @NotBlank @Size(max=120) String name, @Size(max=4000) String description,
        @NotBlank @Size(max=255) String address, @NotBlank @Size(max=120) String locality,
        @NotBlank @Size(max=120) String city, @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @NotNull GenderType genderType, boolean coLiving, @NotBlank @Size(max=120) String contactName,
        @NotBlank @Pattern(regexp="^[0-9+\\- ]{7,20}$") String contactPhone,
        @NotBlank @Email @Size(max=254) String contactEmail, @Size(max=2000) String rules,
        @Size(max=2000) String amenities,
        @Size(max=20) List<@NotBlank @Size(max=1000) @Pattern(regexp="^https?://[^\\s]+$") String> imageUrls,
        @Size(max=30) List<@NotNull @Valid CollegeLinkDto> colleges,
        @DecimalMin("0.0") @Digits(integer=17, fraction=2) BigDecimal securityDeposit,
        @DecimalMin("0.0") @Digits(integer=17, fraction=2) BigDecimal maintenanceChargePerMonth,
        @DecimalMin("0.0") @Digits(integer=17, fraction=2) BigDecimal messChargePerMonth, ElectricityPolicy electricityPolicy,
        @DecimalMin("0.0") @Digits(integer=17, fraction=2) BigDecimal electricityChargePerMonth,
        @Min(0) @Max(120) Integer lockInMonths, @Min(0) @Max(3650) Integer noticePeriodDays, Boolean messIncludedInRent,
        FoodType foodType, @Size(max=500) String mealTimings, @Size(max=7) List<@Valid MessMenuItemDto> weeklyMenu,
        @Pattern(regexp="^[0-9+\\- ]{7,20}$") String contactWhatsapp) {}

    public record HostelCardDto(Long id, String name, String locality, GenderType genderType,
                                boolean coLiving, BigDecimal startingPricePerMonth, int vacantBeds,
                                Double distanceKm, List<String> amenities, List<String> imageUrls,
                                List<AcType> roomTypes) {}
    public record RoomDto(Long id, String roomNumber, AcType acType, int capacity, int occupied,
                          int vacantBeds, BigDecimal pricePerMonth, BigDecimal pricePerDay,
                          String description, String amenities, List<String> imageUrls,
                          BigDecimal firstMonthTotal, List<CostLineDto> costBreakdown, boolean breakdownComplete,
                          java.time.Instant availabilityUpdatedAt) {}
    public record OccupancyChangeRequest(@NotNull Integer delta) {}
    public record CostLineDto(String label, BigDecimal amount) {}
    public record HostelDetailDto(Long id, String name, String description, String address,
                                  String locality, String city, Double latitude, Double longitude,
                                  GenderType genderType, boolean coLiving, String contactName,
                                  String contactPhone, String contactEmail, String rules,
                                  List<String> amenities, List<String> imageUrls,
                                  List<CollegeLinkDto> colleges, List<RoomDto> rooms,
                                  BigDecimal securityDeposit, BigDecimal maintenanceChargePerMonth,
                                  BigDecimal messChargePerMonth, ElectricityPolicy electricityPolicy,
                                  BigDecimal electricityChargePerMonth, Integer lockInMonths, Integer noticePeriodDays,
                                  Boolean messIncludedInRent, FoodType foodType, String mealTimings,
                                  List<MessMenuItemDto> weeklyMenu, String contactWhatsapp) {}
    public record MessMenuItemDto(java.time.DayOfWeek dayOfWeek, @Size(max=200) String breakfast,
                                  @Size(max=200) String lunch, @Size(max=200) String snacks,
                                  @Size(max=200) String dinner) {}

    public record RoomRequest(
        @NotBlank @Size(max=64) String roomNumber, @NotNull AcType acType, @Min(1) @Max(1000) int capacity,
        @Min(0) int occupied, @DecimalMin("0.01") @Digits(integer=17, fraction=2) BigDecimal pricePerMonth,
        @DecimalMin("0.01") @Digits(integer=17, fraction=2) BigDecimal pricePerDay, @Size(max=1000) String description,
        @Size(max=2000) String amenities,
        @Size(max=10) List<@NotBlank @Size(max=300000) @Pattern(regexp="^(https?://[^\\s]+|data:image/jpeg;base64,[A-Za-z0-9+/=]+)$") String> imageUrls) {}
    public record BookingCreateRequest(@NotBlank @Size(max=120) String studentName,
                                       @NotBlank @Pattern(regexp="^[0-9+\\- ]{7,20}$") String phone,
                                       @NotBlank @Email @Size(max=254) String email,
                                       @NotNull @FutureOrPresent LocalDate moveInDate) {}
    public record BookingDto(Long id, Long roomId, String hostelName, String roomNumber,
                             String studentName, String phone, String email, LocalDate moveInDate,
                             BookingStatus status, LocalDateTime createdAt) {}
}
