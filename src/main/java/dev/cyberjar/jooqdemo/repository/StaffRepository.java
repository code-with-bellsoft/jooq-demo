package dev.cyberjar.jooqdemo.repository;

import dev.cyberjar.jooqdemo.dto.StaffDto;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.cyberjar.jooqdemo.Tables.*;


@Repository
public class StaffRepository {

    private final DSLContext context;

    public StaffRepository(DSLContext context) {
        this.context = context;
    }


    public List<StaffDto> findStaffBySpecialtyIdExplicitJoin(Long specialtyId) {

        return context.select(
                        STAFF.ID,
                        STAFF.HANDLE,
                        STAFF.ACTIVE,
                        SPECIALTY.NAME,
                        FACILITY.NAME,
                        FACILITY.TYPE,
                        DISTRICT.NAME
                )
                .from(STAFF)
                .join(SPECIALTY).on(SPECIALTY.ID.eq(STAFF.SPECIALTY_ID))
                .join(FACILITY).on(FACILITY.ID.eq(STAFF.FACILITY_ID))
                .join(DISTRICT).on(DISTRICT.ID.eq(FACILITY.DISTRICT_ID))
                .where(STAFF.SPECIALTY_ID.eq(specialtyId))
                .and(STAFF.ACTIVE.eq(true))
                .orderBy(FACILITY.NAME.asc())
                .fetch(staffRecord -> new StaffDto(
                        staffRecord.get(STAFF.ID),
                        staffRecord.get(STAFF.HANDLE),
                        staffRecord.get(STAFF.ACTIVE),
                        staffRecord.get(SPECIALTY.NAME),
                        staffRecord.get(FACILITY.NAME),
                        staffRecord.get(FACILITY.TYPE).toString(),
                        staffRecord.get(DISTRICT.NAME)
                ));


    }

    public List<StaffDto> findStaffBySpecialtyIdImplicitJoin(Long specialtyId) {

        return context.select(
                        STAFF.ID,
                        STAFF.HANDLE,
                        STAFF.ACTIVE,
                        STAFF.specialty().NAME,
                        STAFF.facility().NAME,
                        STAFF.facility().TYPE,
                        STAFF.facility().district().NAME
                )
                .from(STAFF)
                .where(STAFF.SPECIALTY_ID.eq(specialtyId))
                .and(STAFF.ACTIVE.eq(true))
                .orderBy(STAFF.facility().NAME.asc())
                .fetch(staffRecord -> new StaffDto(
                        staffRecord.get(STAFF.ID),
                        staffRecord.get(STAFF.HANDLE),
                        staffRecord.get(STAFF.ACTIVE),
                        staffRecord.get(STAFF.specialty().NAME),
                        staffRecord.get(STAFF.facility().NAME),
                        staffRecord.get(FACILITY.TYPE).toString(),
                        staffRecord.get(STAFF.facility().district().NAME)
                ));

    }


}
