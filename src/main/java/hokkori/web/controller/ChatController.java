package hokkori.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import hokkori.web.entity.ChannelMaster;
import hokkori.web.repository.ChannelMasterRepository;

@Controller
public class ChatController {
	@Autowired
	private ChannelMasterRepository channelRepository;

	@GetMapping("/chatHtml/{channelId}")
	public String index(@PathVariable String channelId, Model model) {
		ChannelMaster channel = channelRepository.findByChannelId(channelId);
		model.addAttribute("channelName", channel.getChannelName());
		model.addAttribute("channelId", channelId);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String name = "";
		if (authentication != null && authentication.isAuthenticated()) {
			name = authentication.getName();
		}
		model.addAttribute("name", name);
		return "chat";
	}
}
