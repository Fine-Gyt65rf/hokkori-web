package hokkori.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import hokkori.web.service.ChatService;
import hokkori.web.service.MemberService;

@Controller
public class IndexController {
	@Autowired
	private MemberService memberService;
	@Autowired
	private ChatService chatService;
	
	@GetMapping("/")
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String name = "";
		if (authentication != null && authentication.isAuthenticated()) {
			name = authentication.getName();
		}
		if (!name.isEmpty()) {
			
		}
		return "index";
	}
}
