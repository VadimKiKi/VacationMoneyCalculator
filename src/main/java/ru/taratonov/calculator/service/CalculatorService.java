package ru.taratonov.calculator.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.taratonov.calculator.dto.ResponseDto;
import ru.taratonov.calculator.exception.InvalidDateRangeException;
import ru.taratonov.calculator.exception.NumberOfVacationDaysException;
import ru.taratonov.calculator.exception.ValueLessThanZeroException;
import ru.taratonov.calculator.exception.WrongYearException;
import ru.taratonov.calculator.util.HolidayManager;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class CalculatorService {

    private final HolidayManager holidayManager;
    private final double MINIMUM_WAGE = 16_242.0;

    @Autowired
    public CalculatorService(HolidayManager holidayManager) {
        this.holidayManager = holidayManager;
    }

    public ResponseDto getVacationMoney(int numOfVacationDays, double averageYearSalary) {
        if (numOfVacationDays <= 0 || averageYearSalary <= 0)
            throw new ValueLessThanZeroException();
        else if (numOfVacationDays > 28) {
            throw new NumberOfVacationDaysException();
        } else {
            double averageMonthSalary = averageYearSalary / 12;
            double scale = Math.pow(10, 2);
            ResponseDto response = new ResponseDto();
            double vacationMoney;
            if (Double.compare(averageMonthSalary, MINIMUM_WAGE) <= 0) {
                vacationMoney = MINIMUM_WAGE / 29.3 * numOfVacationDays;
                response.setDescription("Your average month salary less than minimum wage. " +
                        "The minimum wage will be used to calculate vacation money!");
            } else {
                vacationMoney = averageMonthSalary / 29.3 * numOfVacationDays;
                response.setDescription("Vacation money was successfully calculated!");
            }
            double personalIncomeTax = Math.round(vacationMoney * 0.13);
            response.setAmountVacationMoney(Math.round(vacationMoney * scale) / scale);
            response.setPersonalIncomeTax(Math.round(personalIncomeTax * scale) / scale);
            response.setTotalAmountVacationMoney(Math.round((vacationMoney - personalIncomeTax) * scale) / scale);
            return response;
        }
    }

    public ResponseDto getVacationMoney(double averageYearSalary, LocalDate startDayVacation, LocalDate endDayVacation) {
        if (endDayVacation.getYear() != 2023 || startDayVacation.getYear() != 2023) {
            throw new WrongYearException();
        } else if (endDayVacation.isBefore(startDayVacation)) {
            throw new InvalidDateRangeException();
        } else {
            int vacationDays = holidayManager.countVacationDays(
                    startDayVacation.datesUntil(endDayVacation.plusDays(1)).collect(Collectors.toList()));
            return getVacationMoney(vacationDays, averageYearSalary);
        }
    }
}
