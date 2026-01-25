package dev.cyberjar.jooqdemo.repository;

import dev.cyberjar.jooqdemo.dto.SlotDto;
import dev.cyberjar.jooqdemo.filter.SlotFilter;
import dev.cyberjar.jooqdemo.enums.BookingStatus;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dev.cyberjar.jooqdemo.Tables.*;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.noCondition;

@Repository
public class AppointmentSlotRepository {

    private final DSLContext context;

    public AppointmentSlotRepository(DSLContext context) {
        this.context = context;
    }

    public Optional<SlotDto> findSlotById(Long id) {
        return context.select(
                        APPOINTMENT_SLOT.ID,
                        FACILITY.ID,
                        FACILITY.NAME,
                        SPECIALTY.ID,
                        SPECIALTY.NAME,
                        APPOINTMENT_SLOT.STARTS_AT,
                        APPOINTMENT_SLOT.ENDS_AT,
                        APPOINTMENT_SLOT.CAPACITY
                )
                .from(APPOINTMENT_SLOT)
                .join(FACILITY).on(FACILITY.ID.eq(APPOINTMENT_SLOT.FACILITY_ID))
                .join(SPECIALTY).on(SPECIALTY.ID.eq(APPOINTMENT_SLOT.SPECIALTY_ID))
                .where(APPOINTMENT_SLOT.ID.eq(id))
                .fetchOptional(slotRecord -> new SlotDto(
                        slotRecord.get(APPOINTMENT_SLOT.ID),
                        slotRecord.get(FACILITY.ID),
                        slotRecord.get(FACILITY.NAME),
                        slotRecord.get(SPECIALTY.ID),
                        slotRecord.get(SPECIALTY.NAME),
                        slotRecord.get(APPOINTMENT_SLOT.STARTS_AT),
                        slotRecord.get(APPOINTMENT_SLOT.ENDS_AT),
                        slotRecord.get(APPOINTMENT_SLOT.CAPACITY)
                ));
    }

    public Long createSlot(SlotDto slotDto) {

        return Objects.requireNonNull(
                        context.insertInto(APPOINTMENT_SLOT)
                                .set(APPOINTMENT_SLOT.FACILITY_ID, slotDto.facilityId())
                                .set(APPOINTMENT_SLOT.SPECIALTY_ID, slotDto.specialtyId())
                                .set(APPOINTMENT_SLOT.STARTS_AT, slotDto.startsAt())
                                .set(APPOINTMENT_SLOT.ENDS_AT, slotDto.endsAt())
                                .set(APPOINTMENT_SLOT.CAPACITY, slotDto.capacity())
                                .returningResult(APPOINTMENT_SLOT.ID)
                                .fetchOne())
                .getValue(APPOINTMENT_SLOT.ID);

    }

    public int updateSlot(Long slotId, int newCapacity) {

        return context.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            SlotLockState state = lockSlotAndCountActiveBookings(ctx, slotId);

            if (!state.exists()) return 0;


            if (newCapacity < state.activeBookings()) {
                throw new IllegalStateException(
                        "Cannot set capacity to " + newCapacity +
                                " because there are " + state.activeBookings() +
                                " active bookings (RESERVED/CONFIRMED)."
                );
            }


            return ctx.update(APPOINTMENT_SLOT)
                    .set(APPOINTMENT_SLOT.CAPACITY, newCapacity)
                    .where(APPOINTMENT_SLOT.ID.eq(slotId))
                    .execute();
        });

    }

    public int deleteSlot(Long slotId) {

        return context.transactionResult(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            SlotLockState state = lockSlotAndCountActiveBookings(ctx, slotId);

            if (!state.exists()) return 0;


            if (state.activeBookings() > 0) {
                throw new IllegalStateException(
                        "Cannot delete slot because there are " + state.activeBookings() +
                                " active bookings (RESERVED/CONFIRMED)."
                );
            }


            return ctx.deleteFrom(APPOINTMENT_SLOT)
                    .where(APPOINTMENT_SLOT.ID.eq(slotId))
                    .execute();
        });
    }

    private SlotLockState lockSlotAndCountActiveBookings(DSLContext ctx, Long slotId) {

        Record1<Long> slot = ctx.select(APPOINTMENT_SLOT.ID)
                .from(APPOINTMENT_SLOT)
                .where(APPOINTMENT_SLOT.ID.eq(slotId))
                .forUpdate()
                .fetchOne();

        if (slot == null) {
            return SlotLockState.missing();
        }

        int active = ctx.fetchCount(
                BOOKING,
                BOOKING.APPOINTMENT_SLOT_ID.eq(slotId),
                BOOKING.STATUS.in(BookingStatus.RESERVED, BookingStatus.CONFIRMED)
        );

        return SlotLockState.present(active);
    }

    private record SlotLockState(boolean exists, int activeBookings) {

        static SlotLockState missing() {
            return new SlotLockState(false, 0);
        }

        static SlotLockState present(int activeBookings) {
            return new SlotLockState(true, activeBookings);
        }
    }

    public List<SlotDto> findSlots(SlotFilter filter) {

        boolean hasFacility = filter.facilityId() != null;
        boolean hasDistrict = filter.districtId() != null;

        return context.select(
                        APPOINTMENT_SLOT.ID,
                        FACILITY.ID,
                        FACILITY.NAME,
                        SPECIALTY.ID,
                        SPECIALTY.NAME,
                        APPOINTMENT_SLOT.STARTS_AT,
                        APPOINTMENT_SLOT.ENDS_AT,
                        APPOINTMENT_SLOT.CAPACITY
                )
                .from(APPOINTMENT_SLOT)
                .join(FACILITY).on(FACILITY.ID.eq(APPOINTMENT_SLOT.FACILITY_ID))
                .join(SPECIALTY).on(SPECIALTY.ID.eq(APPOINTMENT_SLOT.SPECIALTY_ID))
                .leftJoin(BOOKING).on(
                        BOOKING.APPOINTMENT_SLOT_ID.eq(APPOINTMENT_SLOT.ID)
                                .and(BOOKING.STATUS.in(BookingStatus.RESERVED, BookingStatus.CONFIRMED))
                )
                .where(SPECIALTY.ID.eq(filter.specialtyId()))
                .and(hasFacility ? FACILITY.ID.eq(filter.facilityId()) : noCondition())
                .and(hasDistrict ? FACILITY.DISTRICT_ID.eq(filter.districtId()) : noCondition())
                .groupBy(
                        APPOINTMENT_SLOT.ID,
                        FACILITY.ID,
                        FACILITY.NAME,
                        SPECIALTY.ID,
                        SPECIALTY.NAME,
                        APPOINTMENT_SLOT.STARTS_AT,
                        APPOINTMENT_SLOT.ENDS_AT,
                        APPOINTMENT_SLOT.CAPACITY
                )
                .having(count(BOOKING.ID).lt(APPOINTMENT_SLOT.CAPACITY))
                .orderBy(APPOINTMENT_SLOT.STARTS_AT.asc())
                .fetch(slotRecord -> new SlotDto(
                        slotRecord.get(APPOINTMENT_SLOT.ID),
                        slotRecord.get(FACILITY.ID),
                        slotRecord.get(FACILITY.NAME),
                        slotRecord.get(SPECIALTY.ID),
                        slotRecord.get(SPECIALTY.NAME),
                        slotRecord.get(APPOINTMENT_SLOT.STARTS_AT),
                        slotRecord.get(APPOINTMENT_SLOT.ENDS_AT),
                        slotRecord.get(APPOINTMENT_SLOT.CAPACITY)
                ));
    }

}
