package com.dmm.task.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

	@RequestMapping("/create")
	public String create() {
		return "create";
	}

	@RequestMapping("/edit")
	public String edit() {
		return "edit";
	}

	@RequestMapping("/main")
	public String main() {
		return "main";
	}

	@RequestMapping("/login")
	public String login() {
		return "login";
	}
	
	@RequestMapping("/adminMain")
	public String adminMain() {
		return "adminMain";
	}
	
	
}
