package com.dmm.task.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TaskRepository;

@Service
public class CalendarService {

	
    @Autowired
    private TaskRepository taskRepository;
 
    public List<Tasks> getTasksForMonthAndUser(LocalDateTime start, LocalDateTime end, String name) {
        return taskRepository.findByDateBetween(start, end, name);
    }
    

    private List<List<LocalDate>> generateCalendarMatrix() {
        List<List<LocalDate>> matrix = new ArrayList<>();
        
        LocalDate currentDate = LocalDate.now();
        LocalDate firstDayOfMonth = currentDate.with(TemporalAdjusters.firstDayOfMonth());
        int daysInMonth = firstDayOfMonth.lengthOfMonth();
        DayOfWeek firstDayOfWeek = firstDayOfMonth.getDayOfWeek();
        
        LocalDate currentWeekStart = firstDayOfMonth.minusDays(firstDayOfWeek.getValue() - 1);
        
        while (currentWeekStart.getDayOfMonth() <= daysInMonth) {
            List<LocalDate> week = new ArrayList<>();
            
            for (int i = 0; i < 7; i++) {
                week.add(currentWeekStart);
                currentWeekStart = currentWeekStart.plusDays(1);
            }
            
            matrix.add(week);
        }
        
        return matrix;
	    }

	   
}
