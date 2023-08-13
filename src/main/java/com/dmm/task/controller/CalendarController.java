package com.dmm.task.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TaskRepository;
import com.dmm.task.form.TaskForm;
import com.dmm.task.service.AccountUserDetails;
import com.dmm.task.service.CalendarService;

@Controller
public class CalendarController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/main")
    @PreAuthorize("hasRole('ROLE_USER')") 
    public String showCalendar(@RequestParam(name = "date", required = false) String dateString, Model model, @AuthenticationPrincipal AccountUserDetails user) {
        LocalDate date;
        if (dateString == null || dateString.isEmpty()) {
            date = LocalDate.now();
        } else {
            date = LocalDate.parse(dateString);
        }

        Month month = date.getMonth();
        model.addAttribute("month", month);
        
        LocalDate prev = date.minusMonths(1);
        LocalDate next = date.plusMonths(1);

  
        // ログインユーザーに関連するタスクの取得
        List<Tasks> userTasks = taskRepository.findByName(user.getName()); // user.getUser() はユーザーエンティティにアクセスする例です

        // カレンダーにタスク情報を追加
        Map<LocalDate, List<Tasks>> tasksByDate = userTasks.stream()
                .collect(Collectors.groupingBy(task -> task.getDate().toLocalDate()));
        model.addAttribute("tasks", tasksByDate);
        
        
        List<List<LocalDate>> matrix = CalendarService.generateCalendarMatrix(date.getYear(), date.getMonthValue());
        model.addAttribute("matrix", matrix);
        
        model.addAttribute("prev", prev);
        model.addAttribute("next", next);

        return "main";
    }
    
    
	@Autowired
	private TaskRepository repo;
    
	@GetMapping("/main/create")
	public String tasks(Model model) {
		// 逆順で投稿をすべて取得する
		List<Tasks> list = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
//    Collections.reverse(list); //普通に取得してこちらの処理でもOK
		model.addAttribute("posts", list);
		TaskForm taskForm = new TaskForm();
		model.addAttribute("taskForm", taskForm);
		return "/create";
	}
    
	
    @GetMapping("/main/create/{date}")
    public String showCreateFormWithDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Model model) {
    	TaskForm taskForm = new TaskForm();
        taskForm.setSelectedDate(date); // selectedDate を設定
        model.addAttribute("taskForm", taskForm);
        return "create";
    }

    
	@PostMapping("/main/create")
	public String create(@Validated TaskForm taskForm, BindingResult bindingResult,
	        @AuthenticationPrincipal AccountUserDetails user, @RequestParam(name = "selectedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate, Model model) {
		// バリデーションの結果、エラーがあるかどうかチェック
		if (bindingResult.hasErrors()) {
			// エラーがある場合は投稿登録画面を返す
			List<Tasks> list = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
			model.addAttribute("tasks", list);
			model.addAttribute("taskForm", taskForm);
			model.addAttribute("selectedDate", selectedDate); 
			return "/create";
		}
		
		LocalDateTime dateTime = selectedDate.atStartOfDay();

		Tasks post = new Tasks();
		post.setName(user.getName());
		post.setTitle(taskForm.getTitle());
		post.setText(taskForm.getText());
		post.setDate(selectedDate.atStartOfDay());
		post.setDone(false);
		
		repo.save(post);

		return "redirect:/main";
	}
}