package com.engineerbiz.finance.mapper;

import com.engineerbiz.finance.dto.ExpenseResponse;
import com.engineerbiz.finance.entity.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectCode", source = "project.projectCode")
    @Mapping(target = "projectName", source = "project.name")
    ExpenseResponse toResponse(Expense expense);

    List<ExpenseResponse> toResponseList(List<Expense> expenses);
}
