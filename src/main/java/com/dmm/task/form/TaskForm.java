package com.dmm.task.form;

import java.time.LocalDate;

import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class TaskForm {
	// titleへのバリデーション設定を追加
	@Size(min = 1, max = 200)
	private String title;
	
	private LocalDate date;
	
	// textへのバリデーション設定を追加
	@Size(min = 1, max = 200)
	private String text;

}