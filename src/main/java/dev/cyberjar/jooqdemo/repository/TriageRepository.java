package dev.cyberjar.jooqdemo.repository;

import dev.cyberjar.jooqdemo.dto.BookingDto;
import dev.cyberjar.jooqdemo.dto.LabOrderDto;
import dev.cyberjar.jooqdemo.dto.LabResultDto;
import dev.cyberjar.jooqdemo.dto.TriageCaseDetailsDto;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static dev.cyberjar.jooqdemo.Tables.*;
import static org.jooq.impl.DSL.*;

@Repository
public class TriageRepository {

    private final DSLContext context;

    public TriageRepository(DSLContext context) {
        this.context = context;
    }

    public Optional<TriageCaseDetailsDto> findTriageCase(Long triageCaseId) {

        var slotFacility = FACILITY.as("slot_facility");
        var labFacility = FACILITY.as("lab_facility");

        Field<List<BookingDto>> bookings =
                multiset(
                        select(
                                BOOKING.ID.as("booking_id"),
                                BOOKING.STATUS.as("booking_status"),
                                BOOKING.CREATED_AT.as("booking_created_at"),
                                slotFacility.NAME.as("slot_facility_name"),
                                APPOINTMENT_SLOT.STARTS_AT.as("slot_starts_at"),
                                STAFF.HANDLE.as("staff_handle")
                        )
                                .from(BOOKING)
                                .join(APPOINTMENT_SLOT).on(APPOINTMENT_SLOT.ID.eq(BOOKING.APPOINTMENT_SLOT_ID))
                                .join(slotFacility).on(slotFacility.ID.eq(APPOINTMENT_SLOT.FACILITY_ID))
                                .leftJoin(STAFF).on(STAFF.ID.eq(BOOKING.STAFF_ID))
                                .where(BOOKING.TRIAGE_CASE_ID.eq(TRIAGE_CASE.ID))
                                .orderBy(APPOINTMENT_SLOT.STARTS_AT.asc(), BOOKING.CREATED_AT.asc())
                ).as("bookings")
                        .convertFrom(rs -> rs.map(bookingRecord -> new BookingDto(
                                bookingRecord.get(field("booking_id", Long.class)),

                                Optional.ofNullable(bookingRecord.get(field("booking_status"))).map(Object::toString).orElse(null),
                                bookingRecord.get(field("booking_created_at", OffsetDateTime.class)),
                                bookingRecord.get(field("slot_facility_name", String.class)),
                                bookingRecord.get(field("slot_starts_at", OffsetDateTime.class)),
                                bookingRecord.get(field("staff_handle", String.class))
                        )));

        Field<List<LabOrderDto>> labOrders =
                multiset(
                        select(
                                LAB_ORDER.ID.as("lab_order_id"),
                                LAB_ORDER.TEST_CODE.as("test_code"),
                                labFacility.NAME.as("lab_facility_name"),
                                LAB_ORDER.ORDERED_AT.as("ordered_at"),

                                multiset(
                                        select(
                                                LAB_RESULT.RESULT_STATUS.as("result_status"),
                                                LAB_RESULT.PUBLISHED_AT.as("published_at")
                                        )
                                                .from(LAB_RESULT)
                                                .where(LAB_RESULT.LAB_ORDER_ID.eq(LAB_ORDER.ID))
                                                .orderBy(LAB_RESULT.PUBLISHED_AT.asc().nullsLast())
                                ).as("results")
                                        .convertFrom(r2 -> r2.map(rr -> new LabResultDto(
                                                Optional.ofNullable(rr.get(field("result_status"))).map(Object::toString).orElse(null),
                                                rr.get(field("published_at", OffsetDateTime.class))
                                        )))
                        )
                                .from(LAB_ORDER)
                                .join(labFacility).on(labFacility.ID.eq(LAB_ORDER.LAB_FACILITY_ID))
                                .where(LAB_ORDER.TRIAGE_CASE_ID.eq(TRIAGE_CASE.ID))
                                .orderBy(LAB_ORDER.ORDERED_AT.asc())
                ).as("lab_orders")
                        .convertFrom(rs -> rs.map(labOrderRecord -> new LabOrderDto(
                                labOrderRecord.get(field("lab_order_id", Long.class)),
                                labOrderRecord.get(field("test_code", String.class)),
                                labOrderRecord.get(field("lab_facility_name", String.class)),
                                labOrderRecord.get(field("ordered_at", OffsetDateTime.class)),
                                labOrderRecord.get(field("results", List.class))
                        )));

        return context.select(
                        TRIAGE_CASE.ID,
                        TRIAGE_CASE.STATUS,
                        TRIAGE_CASE.CREATED_AT,
                        TRIAGE_CASE.SEVERITY,
                        PATIENT.PUBLIC_REF,
                        FACILITY.NAME.as("intake_facility_name"),
                        SPECIALTY.NAME.as("required_specialty_name"),
                        bookings,
                        labOrders
                )
                .from(TRIAGE_CASE)
                .join(PATIENT).on(PATIENT.ID.eq(TRIAGE_CASE.PATIENT_ID))
                .join(FACILITY).on(FACILITY.ID.eq(TRIAGE_CASE.INTAKE_FACILITY_ID))
                .join(SPECIALTY).on(SPECIALTY.ID.eq(TRIAGE_CASE.REQUIRED_SPECIALTY_ID))
                .where(TRIAGE_CASE.ID.eq(triageCaseId))
                .fetchOptional(triageRecord -> new TriageCaseDetailsDto(
                        triageRecord.get(TRIAGE_CASE.ID),
                        Optional.ofNullable(triageRecord.get(TRIAGE_CASE.STATUS)).map(Object::toString).orElse(null),
                        triageRecord.get(TRIAGE_CASE.CREATED_AT),
                        triageRecord.get(TRIAGE_CASE.SEVERITY),
                        triageRecord.get(PATIENT.PUBLIC_REF),
                        triageRecord.get(field("intake_facility_name", String.class)),
                        triageRecord.get(field("required_specialty_name", String.class)),
                        triageRecord.get(bookings),
                        triageRecord.get(labOrders)
                ));

    }


}
