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
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Repository
public class TriageRepository {

    private final DSLContext context;

    public TriageRepository(DSLContext context) {
        this.context = context;
    }


    /*

    how to use multisets to collect the results of a non-scalar subquery
    into a single nested collection value

     */

    public Optional<TriageCaseDetailsDto> findTriageCase(Long triageCaseId) {

        var slotFacility = FACILITY.as("slot_facility");
        var labFacility = FACILITY.as("lab_facility");


        Field<String> INTAKE_FACILITY_NAME = FACILITY.NAME.as("intake_facility_name");
        Field<String> REQUIRED_SPECIALTY_NAME = SPECIALTY.NAME.as("required_specialty_name");


        Field<Long> BOOKING_ID = BOOKING.ID.as("booking_id");
        Field<?> BOOKING_STATUS = BOOKING.STATUS.as("booking_status"); // enum type depends on codegen
        Field<OffsetDateTime> BOOKING_CREATED_AT = BOOKING.CREATED_AT.as("booking_created_at");
        Field<String> BOOKING_FACILITY = slotFacility.NAME.as("slot_facility_name");
        Field<OffsetDateTime> BOOKING_SLOT_START_AT = APPOINTMENT_SLOT.STARTS_AT.as("slot_starts_at");
        Field<String> BOOKING_STAFF = STAFF.HANDLE.as("staff_handle");

        Field<?> RESULT_STATUS = LAB_RESULT.RESULT_STATUS.as("result_status"); // enum type depends on codegen
        Field<OffsetDateTime> RESULT_PUBLISHED_AT = LAB_RESULT.PUBLISHED_AT.as("published_at");

        Field<Long> ORDER_ID = LAB_ORDER.ID.as("lab_order_id");
        Field<String> ORDER_TEST_CODE = LAB_ORDER.TEST_CODE.as("test_code");
        Field<String> ORDER_LAB_FACILITY = labFacility.NAME.as("lab_facility_name");
        Field<OffsetDateTime> ORDER_ORDERED_AT = LAB_ORDER.ORDERED_AT.as("ordered_at");


        Field<List<BookingDto>> bookings =
                multiset(
                        select(
                                BOOKING_ID,
                                BOOKING_STATUS,
                                BOOKING_CREATED_AT,
                                BOOKING_FACILITY,
                                BOOKING_SLOT_START_AT,
                                BOOKING_STAFF
                        )
                                .from(BOOKING)
                                .join(APPOINTMENT_SLOT).on(APPOINTMENT_SLOT.ID.eq(BOOKING.APPOINTMENT_SLOT_ID))
                                .join(slotFacility).on(slotFacility.ID.eq(APPOINTMENT_SLOT.FACILITY_ID))
                                .leftJoin(STAFF).on(STAFF.ID.eq(BOOKING.STAFF_ID))
                                .where(BOOKING.TRIAGE_CASE_ID.eq(TRIAGE_CASE.ID))
                                .orderBy(APPOINTMENT_SLOT.STARTS_AT.asc(), BOOKING.CREATED_AT.asc())
                ).as("bookings")
                        .convertFrom(rs -> rs.map(bookingRecord -> new BookingDto(
                                bookingRecord.get(BOOKING_ID),

                                bookingRecord.get(BOOKING_STATUS).toString(),
                                bookingRecord.get(BOOKING_CREATED_AT),
                                bookingRecord.get(BOOKING_FACILITY),
                                bookingRecord.get(BOOKING_SLOT_START_AT),
                                bookingRecord.get(BOOKING_STAFF)
                        )));

        Field<List<LabResultDto>> results =
                multiset(
                        select(
                                RESULT_STATUS,
                                RESULT_PUBLISHED_AT
                        )
                                .from(LAB_RESULT)
                                .where(LAB_RESULT.LAB_ORDER_ID.eq(LAB_ORDER.ID))
                                .orderBy(LAB_RESULT.PUBLISHED_AT.asc().nullsLast())
                ).as("results")
                        .convertFrom(rs -> rs.map(resultRecord -> new LabResultDto(
                                String.valueOf(resultRecord.get(RESULT_STATUS)),
                                resultRecord.get(RESULT_PUBLISHED_AT)
                        )));

        Field<List<LabOrderDto>> labOrders =
                multiset(
                        select(
                                ORDER_ID,
                                ORDER_TEST_CODE,
                                ORDER_LAB_FACILITY,
                                ORDER_ORDERED_AT,
                                results)
                                .from(LAB_ORDER)
                                .join(labFacility).on(labFacility.ID.eq(LAB_ORDER.LAB_FACILITY_ID))
                                .where(LAB_ORDER.TRIAGE_CASE_ID.eq(TRIAGE_CASE.ID))
                                .orderBy(LAB_ORDER.ORDERED_AT.asc())
                ).as("lab_orders")
                        .convertFrom(rs -> rs.map(labOrderRecord -> new LabOrderDto(
                                labOrderRecord.get(ORDER_ID),
                                labOrderRecord.get(ORDER_TEST_CODE),
                                labOrderRecord.get(ORDER_LAB_FACILITY),
                                labOrderRecord.get(ORDER_ORDERED_AT),
                                labOrderRecord.get(results)
                        )));

        return context.select(
                        TRIAGE_CASE.ID,
                        TRIAGE_CASE.STATUS,
                        TRIAGE_CASE.CREATED_AT,
                        TRIAGE_CASE.SEVERITY,
                        PATIENT.PUBLIC_REF,
                        INTAKE_FACILITY_NAME,
                        REQUIRED_SPECIALTY_NAME,
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
                        triageRecord.get(INTAKE_FACILITY_NAME),
                        triageRecord.get(REQUIRED_SPECIALTY_NAME),
                        triageRecord.get(bookings),
                        triageRecord.get(labOrders)
                ));

    }


}
