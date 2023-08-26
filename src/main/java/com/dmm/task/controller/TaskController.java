package com.dmm.task.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TaskRepository;
import com.dmm.task.form.TaskForm;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

	@RequestMapping("/login")
	public String login() {
		return "login";
	}
	
    @GetMapping("/main")
     public String showCalendar(@RequestParam(name = "date", required = false) String dateString, Model model, @AuthenticationPrincipal AccountUserDetails user) {
        LocalDate date;
        if (dateString == null || dateString.isEmpty()) {
            date = LocalDate.now();
        } else {
            date = LocalDate.parse(dateString);
        }
        //月
        Month month = date.getMonth();
        model.addAttribute("month", month);

        LocalDate prev = date.minusMonths(1);
        LocalDate next = date.plusMonths(1);
        model.addAttribute("prev", prev);
        model.addAttribute("next", next);
 
        //週
        List<List<LocalDate>> matrix = new ArrayList<>();
        LocalDate startOfWeek = date.withDayOfMonth(1).with(java.time.DayOfWeek.SUNDAY);

        for (int i = 0; i < 6; i++) {
            List<LocalDate> week = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                LocalDate currentDate = startOfWeek.plusDays(i * 7 + j);
                week.add(currentDate);
            }
            matrix.add(week);
        }

        model.addAttribute("matrix", matrix);
        
        //タスクのリストを取得
        
        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = date.withDayOfMonth(date.lengthOfMonth()).atTime(LocalTime.MAX);       
        
                List<Tasks> tasks = taskRepository.findByDateBetween(
                startOfMonth,
                endOfMonth,
                user.getName()
        );
        
        model.addAttribute("tasks", tasks);// 2. タスクのリストをhtmlに追加   
 
        return "main";
    }
 
    @GetMapping("/main/create/{date}")
    public String showCreateFormWithDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Model model) {
    	TaskForm taskForm = new TaskForm();
        taskForm.setDate(date); // selectedDate を設定
        model.addAttribute("taskForm", taskForm);
        return "create";
    }
    
	// マッピング設定
	@PostMapping("/main/create")
	public String create (@Validated TaskForm taskForm, BindingResult bindingResult,
			@AuthenticationPrincipal AccountUserDetails user, Model model) {
		if (bindingResult.hasErrors()) {
			List<Tasks> list = taskRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
		    model.addAttribute("tasks", list);
			model.addAttribute("taskForm", taskForm);
			return "main";
			}

		Tasks post = new Tasks();
		
        LocalDateTime localDate = post.getDate();
		
		post.setName(user.getName());
		post.setTitle(taskForm.getTitle());
		post.setText(taskForm.getText());
		post.setDate(localDate);
		post.setDone(false);
		
		// データベースに保存
		taskRepository.save(post);
		
		// カレンダー画面へリダイレクト
		return "redirect:/main";
	}


    
}