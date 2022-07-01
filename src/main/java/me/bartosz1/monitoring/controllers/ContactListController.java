package me.bartosz1.monitoring.controllers;

import me.bartosz1.monitoring.models.*;
import me.bartosz1.monitoring.repos.ContactListRepository;
import me.bartosz1.monitoring.repos.MonitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/contactlist")
public class ContactListController {

    @Autowired
    private ContactListRepository contactListRepository;
    @Autowired
    private MonitorRepository monitorRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ContactListController.class);

    @RequestMapping(method = RequestMethod.POST, path = "/add", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> addContactList(HttpServletRequest req, @AuthenticationPrincipal User user, @RequestBody ContactListCDO cdo) {
        ContactList contactList = contactListRepository.save(new ContactList(cdo, user));
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/contactlist/add ID: " + contactList.getId());
        return new ResponseEntity<>(new Response("ok").addAdditionalInfo("id", contactList.getId()), HttpStatus.CREATED);

    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/delete", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> deleteContactList(HttpServletRequest req, @AuthenticationPrincipal User user, @RequestParam long id) {
        Optional<ContactList> result = contactListRepository.findById(id);
        if (result.isPresent()) {
            ContactList contactList = result.get();
            if (contactList.getUser().getId() == user.getId()) {
                Iterable<Monitor> monitors = monitorRepository.findAllByContactList(contactList);
                monitors.forEach(monitor -> monitor.setContactList(null));
                monitorRepository.saveAll(monitors);
                contactListRepository.delete(contactList);
                LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/contactlist/delete ID: " + id);
                return new ResponseEntity<>(new Response("ok"), HttpStatus.OK);
            }
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/contactlist/delete ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/get", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getContactList(HttpServletRequest req, @AuthenticationPrincipal User user, @RequestParam long id) {
        Optional<ContactList> result = contactListRepository.findById(id);
        if (result.isPresent()) {
            ContactList contactList = result.get();
            if (contactList.getUser().getId() == user.getId()) {
                LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/contactlist/get ID: " + id);
                return new ResponseEntity<>(new Response("ok").addAdditionalData(contactList), HttpStatus.OK);
            }
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/contactlist/get ID: " + id + " FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/list", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> listContactLists(HttpServletRequest req, @AuthenticationPrincipal User user) {
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/contactlist/list");
        Iterable<ContactList> contactLists = contactListRepository.findAllByUser(user);
        return new ResponseEntity<>(new Response("ok").addAdditionalData(contactLists), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/modify", produces = "application/json")
    @ResponseBody
    //I know that expecting all props isn't too good because you have to fetch them from /get or /list earlier but eh
    //I might remake that later
    public ResponseEntity<?> modifyContactList(HttpServletRequest req, @AuthenticationPrincipal User user, @RequestBody ContactListCDO cdo, @RequestParam long id) {
        Optional<ContactList> result = contactListRepository.findById(id);
        if (result.isPresent()) {
            if (result.get().getUser().getId() == user.getId()) {
                ContactList contactList = new ContactList(cdo, user).setId(id);
                contactListRepository.save(contactList);
                LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/contactlist/modify ID: " + id);
                return new ResponseEntity<>(new Response("ok").addAdditionalData(contactList), HttpStatus.CREATED);
            }
        }
        LOGGER.info(req.getRemoteAddr() + " USER " + user.getId() + " -> /v1/contactlist/modify ID: " + id + "FAIL: not found");
        return new ResponseEntity<>(new Response("not found"), HttpStatus.NOT_FOUND);
    }
}
