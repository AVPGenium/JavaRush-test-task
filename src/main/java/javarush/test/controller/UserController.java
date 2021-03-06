package javarush.test.controller;

import javarush.test.dao.UserDao;
import javarush.test.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController {
    @Autowired
    UserDao userDao;

    @RequestMapping("/")
    public String hello(Model model,
                        @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                        @RequestParam(value = "searchText", required = false) String searchText) throws UnsupportedEncodingException {
        if (pageNumber == null) {
            pageNumber = 0;
        }
        if (searchText != null) {
            List<User> users = userDao.getByName(searchText);
            int pageCount = userDao.getUserPagesCount(users.size());
            model.addAttribute("countOFPages", pageCount);
            model.addAttribute("currentPage", pageNumber);
            model.addAttribute("users", getSubList(users, pageNumber*UserDao.NEWS_ON_PAGE_COUNT, (pageNumber+1)*UserDao.NEWS_ON_PAGE_COUNT));
            model.addAttribute("pagination", createPagination(pageNumber, pageCount));
            model.addAttribute("searchText", searchText);
        } else {
            int pageCount = userDao.getUserPagesCount();
            model.addAttribute("countOFPages", pageCount);
            model.addAttribute("currentPage", pageNumber);
            model.addAttribute("users", userDao.getUsersOnPage(pageNumber));
            model.addAttribute("pagination", createPagination(pageNumber, pageCount));
            model.addAttribute("searchText", "");
        }
        return "index";
    }

    public List<User> getSubList(List<User> list, int startIndex, int endIndex){
        if(list.size() < 1){
            return new ArrayList<User>();
        }
        return list.size() < endIndex ? list.subList(startIndex, list.size()-1) : list.subList(startIndex, endIndex);
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addUserGet(Model model, @RequestParam(value = "id", required = false) Integer userId){
        User user;
        if(userId == null){
            user = new User();
        } else{
            user = userDao.getById(userId);
        }
        model.addAttribute("user", user);
        return "user_add";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addUserPost(Model model, @ModelAttribute User modelUser){
        userDao.saveOrUpdate(modelUser);
        return "redirect:/";
    }

    @RequestMapping("/remove")
    public String removeUser(Model model, @RequestParam(value = "id") Integer userId){
        userDao.delete(userDao.getById(userId));
        return "redirect:/";
    }

    public List<Integer> createPagination(int pageIndex, int pagesCount){
        List<Integer> pageIndexes = new ArrayList<>();
        int leftIndex = pageIndex - 5 >= 0 ? pageIndex - 5 : 0;
        int rightIndex = pageIndex + 5 <= pagesCount ? pageIndex + 5 : pagesCount;
        if(pageIndex - leftIndex < 5){
            rightIndex += 5 - (pageIndex - leftIndex);
            rightIndex = rightIndex <= pagesCount ? rightIndex : pagesCount;
        }
        if(rightIndex - pageIndex < 5){
            leftIndex -= 5 - (rightIndex - pageIndex);
            leftIndex = leftIndex >= 0 ? leftIndex : 0;
        }
        for (int i = leftIndex; i <= rightIndex ; i++) {
            pageIndexes.add(i);
        }
        return pageIndexes;
    }
}
