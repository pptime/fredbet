package de.fred4jupiter.fredbet.web.image;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.fred4jupiter.fredbet.domain.ImageGroup;
import de.fred4jupiter.fredbet.security.FredBetPermission;
import de.fred4jupiter.fredbet.service.image.ImageGroupExistsException;
import de.fred4jupiter.fredbet.service.image.ImageGroupService;
import de.fred4jupiter.fredbet.web.WebMessageUtil;

@Controller
@RequestMapping("/imagegroup")
@PreAuthorize("hasAuthority('" + FredBetPermission.PERM_EDIT_IMAGE_GROUP + "')")
public class ImageGroupController {

	private static final String REDIRECT_SHOW_IMAGEGROUP = "redirect:/imagegroup/show";

	@Autowired
	private ImageGroupService imageGroupService;

	@Autowired
	private WebMessageUtil messageUtil;

	@ModelAttribute("imageGroupCommand")
	public ImageGroupCommand init() {
		return new ImageGroupCommand();
	}

	@ModelAttribute("availableImageGroups")
	public List<ImageGroupCommand> availableImageGroups() {
		List<ImageGroup> imageGroups = imageGroupService.findAvailableImageGroups();
		return imageGroups.stream().map(imageGroup -> mapToImageGroupCommand(imageGroup)).sorted().collect(Collectors.toList());
	}

	private ImageGroupCommand mapToImageGroupCommand(ImageGroup imageGroup) {
		ImageGroupCommand imageGroupCommand = new ImageGroupCommand();
		imageGroupCommand.setId(imageGroup.getId());
		imageGroupCommand.setName(imageGroup.getName());
		return imageGroupCommand;
	}

	@RequestMapping(value = "/show", method = RequestMethod.GET)
	public ModelAndView show() {
		return new ModelAndView("image/image_group");
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ModelAndView deleteImage(@ModelAttribute("imageGroupCommand") ImageGroupCommand imageGroupCommand, RedirectAttributes redirect) {
		try {
			imageGroupService.deleteImageGroup(imageGroupCommand.getId());
			messageUtil.addInfoMsg(redirect, "image.group.msg.deleted");
		} catch (DataIntegrityViolationException e) {
			messageUtil.addErrorMsg(redirect, "image.group.msg.deletionHasReferences");
		}

		return new ModelAndView(REDIRECT_SHOW_IMAGEGROUP);
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ModelAndView addImageGroup(@ModelAttribute("imageGroupCommand") ImageGroupCommand imageGroupCommand, BindingResult result,
			RedirectAttributes redirect) {

		try {
			if (imageGroupCommand.getId() == null) {
				imageGroupService.addImageGroup(imageGroupCommand.getName());
				messageUtil.addInfoMsg(redirect, "image.group.msg.added", imageGroupCommand.getName());
			} else {
				imageGroupService.updateImageGroup(imageGroupCommand.getId(), imageGroupCommand.getName());
				messageUtil.addInfoMsg(redirect, "image.group.msg.updated", imageGroupCommand.getName());
			}

		} catch (ImageGroupExistsException e) {
			messageUtil.addErrorMsg(redirect, "image.group.msg.groupExist", imageGroupCommand.getName());
		}

		return new ModelAndView(REDIRECT_SHOW_IMAGEGROUP);
	}
}
