package dev.cyberjar.jooqdemo.repository;

import dev.cyberjar.jooqdemo.dto.SlotSuggestionDto;
import dev.cyberjar.jooqdemo.enums.BookingStatus;
import org.jooq.*;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

import static dev.cyberjar.jooqdemo.Tables.*;
import static org.jooq.impl.DSL.*;


@Repository
public class SchedulingRepository {

    private final DSLContext context;

    private static final String SLOT_ID_FIELD = "slot_id";
    private static final String SLOT_STARTS_AT_FIELD = "slot_starts_at";
    private static final String SLOT_ENDS_AT_FIELD = "slot_ends_at";
    private static final String DISTRICT_NAME_FIELD = "district_name";
    private static final String FACILITY_NAME_FIELD = "facility_name";
    private static final String SPECIALTY_NAME_FIELD = "specialty_name";
    private static final String CAPACITY_FIELD = "capacity";
    private static final String REMAINING_CAPACITY_FIELD = "remaining_capacity";


    public SchedulingRepository(DSLContext context) {
        this.context = context;
    }

    public List<SlotSuggestionDto> suggestSlots(Long triageCaseId) {


        CommonTableExpression<Record2<Long, Long>> caseContextCte =
                name("case_context")
                        .fields("triage_case_id", "required_specialty_id")
                        .as(
                                select(TRIAGE_CASE.ID,
                                        TRIAGE_CASE.REQUIRED_SPECIALTY_ID)
                                        .from(TRIAGE_CASE)
                                        .where(TRIAGE_CASE.ID.eq(triageCaseId)));

        Table<?> cc = caseContextCte.asTable("cc");

        Field<Long> ccRequiredSpecialtyId = cc.field("required_specialty_id", Long.class);


        CommonTableExpression<?> candidateSlotsCte =
                name("candidate_slots")
                        .fields(SLOT_ID_FIELD,
                                SLOT_STARTS_AT_FIELD,
                                SLOT_ENDS_AT_FIELD,
                                FACILITY_NAME_FIELD,
                                DISTRICT_NAME_FIELD,
                                SPECIALTY_NAME_FIELD,
                                CAPACITY_FIELD
                        ).as(
                                select(
                                        APPOINTMENT_SLOT.ID,
                                        APPOINTMENT_SLOT.STARTS_AT,
                                        APPOINTMENT_SLOT.ENDS_AT,
                                        FACILITY.NAME,
                                        DISTRICT.NAME,
                                        SPECIALTY.NAME,
                                        APPOINTMENT_SLOT.CAPACITY)
                                        .from(cc)
                                        .join(APPOINTMENT_SLOT).on(APPOINTMENT_SLOT.SPECIALTY_ID.eq(ccRequiredSpecialtyId))
                                        .join(FACILITY).on(FACILITY.ID.eq(APPOINTMENT_SLOT.FACILITY_ID))
                                        .join(DISTRICT).on(DISTRICT.ID.eq(FACILITY.DISTRICT_ID))
                                        .join(SPECIALTY).on(SPECIALTY.ID.eq(APPOINTMENT_SLOT.SPECIALTY_ID)));


        Table<?> cs = candidateSlotsCte.asTable("cs");

        Field<Long> csSlotId = cs.field(SLOT_ID_FIELD, Long.class);
        Field<OffsetDateTime> csStart = cs.field(SLOT_STARTS_AT_FIELD, OffsetDateTime.class);
        Field<OffsetDateTime> csEnd = cs.field(SLOT_ENDS_AT_FIELD, OffsetDateTime.class);
        Field<String> csFacilityName = cs.field(FACILITY_NAME_FIELD, String.class);
        Field<String> csDistrictName = cs.field(DISTRICT_NAME_FIELD, String.class);
        Field<String> csSpecialtyName = cs.field(SPECIALTY_NAME_FIELD, String.class);
        Field<Integer> csCapacity = cs.field(CAPACITY_FIELD, Integer.class);

        Field<Integer> activeBookings = selectCount()
                .from(BOOKING)
                .where(BOOKING.APPOINTMENT_SLOT_ID.eq(csSlotId))
                .and(BOOKING.STATUS.in(BookingStatus.RESERVED, BookingStatus.CONFIRMED))
                .asField("active_bookings");

        Field<Integer> remainingCapacity =
                greatest(inline(0), csCapacity.minus(coalesce(activeBookings, 0)))
                        .as(REMAINING_CAPACITY_FIELD);

        CommonTableExpression<?> slotsWithRemainingCapacityCte =
                name("slots_with_capacity")
                        .fields(SLOT_ID_FIELD,
                                SLOT_STARTS_AT_FIELD,
                                SLOT_ENDS_AT_FIELD,
                                FACILITY_NAME_FIELD,
                                DISTRICT_NAME_FIELD,
                                SPECIALTY_NAME_FIELD,
                                REMAINING_CAPACITY_FIELD).as(
                                select(
                                        csSlotId,
                                        csStart,
                                        csEnd,
                                        csFacilityName,
                                        csDistrictName,
                                        csSpecialtyName,
                                        remainingCapacity
                                )
                                        .from(cs));

        Table<?> swc = slotsWithRemainingCapacityCte.asTable("swc");

        Field<Long> swcSlotId = swc.field(SLOT_ID_FIELD, Long.class);
        Field<OffsetDateTime> swcStart = swc.field(SLOT_STARTS_AT_FIELD, OffsetDateTime.class);
        Field<OffsetDateTime> swcEnd = swc.field(SLOT_ENDS_AT_FIELD, OffsetDateTime.class);
        Field<String> swcFacilityName = swc.field(FACILITY_NAME_FIELD, String.class);
        Field<String> swcDistrictName = swc.field(DISTRICT_NAME_FIELD, String.class);
        Field<String> swcSpecialtyName = swc.field(SPECIALTY_NAME_FIELD, String.class);
        Field<Integer> swcRemaining = swc.field(REMAINING_CAPACITY_FIELD, Integer.class);

        return context.with(caseContextCte)
                .with(candidateSlotsCte)
                .with(slotsWithRemainingCapacityCte)
                .select(
                        swcSlotId,
                        swcStart,
                        swcEnd,
                        swcFacilityName,
                        swcDistrictName,
                        swcSpecialtyName,
                        swcRemaining
                )
                .from(swc)
                .where(swcRemaining.gt(0))
                .orderBy(swcStart.asc())
                .limit(50)
                .fetch(slotRecord -> new SlotSuggestionDto(
                        slotRecord.get(swcSlotId),
                        slotRecord.get(swcStart),
                        slotRecord.get(swcEnd),
                        slotRecord.get(swcFacilityName),
                        slotRecord.get(swcDistrictName),
                        slotRecord.get(swcSpecialtyName),
                        slotRecord.get(swcRemaining)
                ));
    }

}
