package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
	public String getCalendar(@AuthenticationPrincipal AccountUserDetails user,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, Model model) {

		List<List<LocalDate>> matrix = new ArrayList<>();
		List<LocalDate> week = new ArrayList<>();

		LocalDate current;
		if (date == null) {
			current = LocalDate.now();
			current = LocalDate.of(current.getYear(), current.getMonthValue(), 1);
		} else {
			current = date;
		}

		//月 
		/*
        Month month = current.getMonth();
        model.addAttribute("month", month);

        LocalDate prev = current.minusMonths(1);
        LocalDate next = current.plusMonths(1);
        model.addAttribute("prev", prev);
        model.addAttribute("next", next);
		*/
		String Str = current.format(DateTimeFormatter.ofPattern("yyyy年MM月"));
		model.addAttribute("month", Str);
		model.addAttribute("prev", current.minusMonths(1));
		model.addAttribute("next", current.plusMonths(1));
        
       //週
		DayOfWeek w = current.getDayOfWeek();
		LocalDate day = current.minusDays(w.getValue());

		for (int i = 1; i <= 7; i++) {
			week.add(day);
			day = day.plusDays(1);
		}
		matrix.add(week);
		week = new ArrayList<>();

		int length = current.lengthOfMonth(); // 日数
		LocalDate lastday = current.withDayOfMonth(length); 

		for (int i = day.getDayOfMonth(); i <= length; i++) {
			DayOfWeek w2 = day.getDayOfWeek();
			week.add(day);
			if (w2 == DayOfWeek.SATURDAY) {
				matrix.add(week);
				week = new ArrayList<>();
			}
			day = day.plusDays(1);
		}

		w = day.getDayOfWeek();
		for (int i = 1; i <= 7 - w.getValue(); i++) {
			week.add(day);
			day = day.plusDays(1);
		}
		matrix.add(week);

		// タスクを取得
		List<Tasks> list;

		if (user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ADMIN"))) {
			list = taskRepository.findByDateBetweenByAdmin(current.atTime(0, 0), lastday.atTime(0, 0));
		} else {
			list = taskRepository.findByDateBetween(current.atTime(0, 0), lastday.atTime(0, 0), user.getName());
		}

		// 取得したデータをtasksに追加する
		MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();
		for (Tasks task : list) {
			LocalDate c = task.getDate().toLocalDate();
			tasks.add(c, task);
		}

		model.addAttribute("matrix", matrix);
		model.addAttribute("tasks", tasks);

		return "/main";
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
			return "/main";
			}

		Tasks task = new Tasks();
		
		task.setName(user.getName());
		task.setTitle(taskForm.getTitle());
		task.setText(taskForm.getText());
		task.setDate(taskForm.getDate().atTime(0, 0));
		task.setDone(false);
		
		// データベースに保存
		taskRepository.save(task);
		
		// カレンダー画面へリダイレクト
		return "redirect:/main";
	}



	@GetMapping("/main/edit/{id}")
	public String Taskid(Model model, @PathVariable Integer id) {
		Tasks task = taskRepository.findById(id).get();
		model.addAttribute("task", task);
		return "edit";
	}

//	Users user = repository.findById(userName).get();
	
	@PostMapping("/main/edit/{id}")
	public String edit(@Validated TaskForm taskForm, BindingResult bindingResult, Model model,
			@PathVariable Integer id) {

		Tasks task = taskRepository.findById(id).get();

		model.addAttribute("task", task);
		
		task.setName(task.getName());
		task.setTitle(taskForm.getTitle());
		task.setText(taskForm.getText());
		task.setDate(taskForm.getDate().atTime(0, 0));
		task.setDone(taskForm.isDone());

		taskRepository.save(task);

		return "redirect:/main";
	}


	@PostMapping("/main/delete/{id}")
	public String delete(@PathVariable Integer id) {
		taskRepository.deleteById(id);
		return "redirect:/main";
	}
	
    
}