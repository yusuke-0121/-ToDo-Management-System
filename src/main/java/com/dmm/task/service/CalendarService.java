package com.dmm.task.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CalendarService {

	   public static List<List<LocalDate>> generateCalendarMatrix(int year, int month) {
	        List<List<LocalDate>> matrix = new ArrayList<>();
	        LocalDate startOfMonth = LocalDate.of(year, month, 1);
	        LocalDate date = startOfMonth.minusDays(startOfMonth.getDayOfWeek().getValue() - DayOfWeek.SUNDAY.getValue());

	        for (int i = 0; i < 6; i++) {
	            List<LocalDate> week = new ArrayList<>();
	            for (int j = 0; j < 7; j++) {
	                week.add(date);
	                date = date.plusDays(1);
	            }
	            matrix.add(week);
	        }

	        return matrix;
	    }
	   
}
