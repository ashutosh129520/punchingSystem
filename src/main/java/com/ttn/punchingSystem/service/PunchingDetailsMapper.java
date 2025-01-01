package com.ttn.punchingSystem.service;

import com.ttn.punchingSystem.model.PunchDetailsWrapper;
import com.ttn.punchingSystem.model.PunchingDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface PunchingDetailsMapper {

    PunchingDetailsMapper INSTANCE = Mappers.getMapper(PunchingDetailsMapper.class);

    @Mapping(source = "key", target = "userEmail")
    @Mapping(expression = "java(entry.getValue().get(0))", target = "punchIn")
    @Mapping(expression = "java(entry.getValue().get(entry.getValue().size() - 1))", target = "punchOut")
    PunchDetailsWrapper mapToWrapper(Map.Entry<String, List<Date>> entry);

    @Mapping(source = "wrapper.userEmail", target = "userEmail")
    @Mapping(source = "wrapper.punchIn", target = "punchDate")
    @Mapping(source = "wrapper.punchIn", target = "punchInTime")
    @Mapping(source = "wrapper.punchOut", target = "punchOutTime")
    void updatePunchingDetails(@MappingTarget PunchingDetails punchingDetails, PunchDetailsWrapper wrapper);
}

