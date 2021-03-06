package clientChat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;

@Controller
@Slf4j
public class GreetingController {
    //  private Logger log;

    static Long id;
    @Autowired
    private SimpMessagingTemplate template;

    //при подписке на topic/greetings вызывает метод message
    @MessageMapping("/hello")
    public void gotMessage(MessageApp message) throws Exception {
        System.out.println("Received " + message.getContent());
        System.out.println("Received Id" + message.getId());
        System.out.println("Received Email " + message.getEmailSender());
        System.out.println("Date " + message.getDate());
        EventApp evap = eventRepository.findById(message.getId()).get();

        ArrayList<String> list = evap.getMessage();
        if (list == null)
            list = new ArrayList<>();
        String dialogue;
        dialogue = message.getEmailSender() + " " + message.getDate();
        dialogue += " " + message.getContent();
        list.add(dialogue);
        evap.setMessage(list);
        eventRepository.save(evap);
        template.convertAndSend("/topic/greeting" + message.getId(), message);
    }


    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private UserRepository userRepository;
    @Autowired
    private EventsRepository eventRepository;
    @Autowired
    private TestRepository testRepository;

    @RequestMapping(value = "/post", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseBody
    public String post(@RequestBody Event image) {
        System.out.println("/POST request with " + image.toString());
        // save Image to C:\\server folder
        String path = "C:\\server\\" + image.getID() + ".png";
        String s = Base64.getEncoder().encodeToString(image.getImage());
        UtilBase64Image.decoder(s, path);
        EventApp event = new EventApp();

        event.setDecription(image.getDescribe());
        event.setName(image.getTitle());
        event.setPosition(image.getPosition());
        event.setImage(image.getImage());
        event.setDate(image.getDate());
        event.setKind(image.getKind());
        event.setTime(image.getTime());
        event.setAddress(image.getAddress());
        event.setArrayThings(image.getArrayThings());
        ArrayList<Long> arrU = null;
        UserApp cur = null;
        for (UserApp u : userRepository.findAll()) {
            if (u.getEmail().equals(image.getAuthor())) {
                ArrayList<String> arr = event.getPeople();
                arr.add(Long.toString(u.getId()));
                event.setPeople(arr);
                arrU = u.getEventsSub();
                cur = u;
                break;
            }
        }
        event.setAuthor(image.getAuthor());
        eventRepository.save(event);
        arrU.add(event.getId());
        cur.setEventsSub(arrU);
        userRepository.save(cur);
        template.convertAndSend("/topic/greeting/eventUpdate",
                new MessageApp("MessageApp for all"));
        // This returns a JSON or XML with the users
        return Long.toString(event.getId());
    }

    //add user
    @GetMapping(path = "/add") // Map ONLY GET Requests
    public @ResponseBody
    String addNewUser(@RequestParam String name
            , @RequestParam String email, @RequestParam String password, @RequestParam String age, @RequestParam String city) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        UserApp n = new UserApp();
        n.setmName(name);
        n.setEmail(email);
        n.setPassword(password);
        n.setAge(age);
        n.setCity(city);
        for (UserApp u : userRepository.findAll()) {
            if (u.getEmail().equals(email))
                return "0";
        }
        userRepository.save(n);
        for (UserApp u : userRepository.findAll()) {
            if (u.getEmail().equals(email))
                return "" + u.getId();
        }
        return "0";
    }


    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }


    @RequestMapping(value = "/postUser", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseBody
    public String postUser(@RequestBody User image) {
        // for(UserApp a:userRepository.findAll()
        System.out.println("/POST request with " + image.toString());
        // save Image to C:\\server folder
        String path = "C:\\server\\" + image.getEmail() + ".png";
        String s = Base64.getEncoder().encodeToString(image.getImage());
        UtilBase64Image.decoder(s, path);
        UserApp user = new UserApp();
        user.setAge(image.getAge());
        user.setCity(image.getCity());
        user.setEmail(image.getEmail());
        user.setmName(image.getName());
        user.setPassword(MD5(image.getPassword()));
        user.setImage(image.getImage());
        userRepository.save(user);
        // This returns a JSON or XML with the users
        return Long.toString(user.getId());
    }


    @RequestMapping(value = "/postUserUpdate", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseBody
    public void postUserUpdate(@RequestBody User image) {
        for (UserApp a : userRepository.findAll()) {
            if (a.getEmail().equals(image.getEmail())) {
                a.setImage(image.getImage());
                a.setmName(image.getName());
                a.setCity(image.getCity());
                a.setAge(image.getAge());
                userRepository.save(a);
                System.out.println("/POST request with " + image.toString());
                // save Image to C:\\server folder
                String path = "C:\\server\\" + image.getEmail() + ".png";
                String s = Base64.getEncoder().encodeToString(image.getImage());
                UtilBase64Image.decoder(s, path);
                return;
            }
        }


    }


    @GetMapping(path = "/updatePerson")
    public @ResponseBody
    void updatePerson(@RequestParam String email, @RequestParam String name, @RequestParam String age, @RequestParam String city) {
        // This returns a JSON or XML with the users
        for (UserApp u : userRepository.findAll()) {
            if (u.getEmail().equals(email)) {
                u.setCity(city);
                u.setAge(age);
                u.setmName(name);
                userRepository.save(u);
            }
        }

    }


    //Id title description place
    @GetMapping(path = "/updateEvent")
    public @ResponseBody
    void updateEvent(@RequestParam String Id, @RequestParam String title, @RequestParam String description, @RequestParam String place) {
        // This returns a JSON or XML with the users
        for (EventApp u : eventRepository.findAll()) {
            if (u.getId().equals(Long.valueOf(Id))) {
                u.setPosition(place);
                u.setName(title);
                u.setDecription(description);
                eventRepository.save(u);
                return;
            }
        }

    }

    @GetMapping(path = "/getMessages")
    public @ResponseBody
    MessageArray getMessages(@RequestParam String Id) {
        // This returns a JSON or XML with the users
        Long id = Long.valueOf(Id);
        EventApp event = eventRepository.findById(id).get();
        ArrayList<String> list = event.getMessage();
        MessageArray result = new MessageArray();
        if (list == null)
            return result;
        ArrayList<Message> answer = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Message message = new Message();
            String email = list.get(i).split(" ")[0];
            String time = list.get(i).split(" ")[1];
            String content = list.get(i).substring(email.length() + time.length() + 2);
            message.setFrom(email);
            message.setMessage(content);
            message.setDate(Long.valueOf(time));
            answer.add(message);
        }

        result.setMessages(answer);
        return result;
    }


    @GetMapping(path = "/loginUser")
    public @ResponseBody
    User loginUser(@RequestParam String email, @RequestParam String password) {
        // This returns a JSON or XML with the users
        for (UserApp u : userRepository.findAll()) {
            if (u.getEmail().equals(email) && (MD5(password).equals(u.getPassword()))) {
                User result = new User();
                result.setName(u.getmName());
                result.setAge(u.getAge());
                result.setCity(u.getCity());
                result.setPassword(u.getPassword());
                result.setEmail(u.getEmail());
                result.setId(Long.toString(u.getId()));
                result.setImage(u.getImage());
                return result;
            }
        }
        return null;
    }


    @GetMapping(path = "/AllEvents")
    public @ResponseBody
    ListEvents eventAllReturn() {
        // This returns a JSON or XML with the users

        ArrayList<Event> result = new ArrayList<>();
        for (EventApp u : eventRepository.findAll()) {
            Event event = new Event(Long.toString(u.getId()), u.getName(), u.getDecription(), u.getAuthor());
            event.setImage(u.getImage());
            event.setDate(u.getDate());
            event.setKind(u.getKind());
            event.setTime(u.getTime());
            event.setPosition(u.getPosition());
            event.setAddress(u.getAddress());
            event.setArrayThings(u.getArrayThings());
            result.add(event);
        }
        ListEvents eal = new ListEvents();
        eal.setListEvent(result);
        return eal;
    }


    @GetMapping(path = "/AllEventsOfUser")
    public @ResponseBody
    ListEvents allEventsOfUser(@RequestParam String email) {
        // This returns a JSON or XML with the users

        ArrayList<Event> result = new ArrayList<>();
        for (UserApp u : userRepository.findAll()) {
            if (u.getEmail().equals(email)) {
                ArrayList<Long> arr = u.getEventsSub();
                for (int i = 0; i < arr.size(); i++) {
                    System.out.println(arr.get(i));
                    EventApp ea = eventRepository.findById(arr.get(i)).get();
                    Event curr = new Event(Long.toString(ea.getId()), ea.getName(), ea.getDecription(), ea.getAuthor());
                    curr.setPosition(ea.getPosition());
                    curr.setTime(ea.getTime());
                    curr.setKind(ea.getKind());
                    curr.setDate(ea.getDate());
                    curr.setImage(ea.getImage());
                    curr.setAddress(ea.getAddress());
                    curr.setArrayThings(ea.getArrayThings());
                    result.add(curr);
                    //  System.out.println(result.size());
                }
                ListEvents eal = new ListEvents();
                eal.setListEvent(result);
                return eal;
            }
        }
        return null;
    }

    //Добавить человеку событие в subscribers
    @GetMapping(path = "/addEventToUser")
    public @ResponseBody
    void addEventToUser(@RequestParam String email, @RequestParam String id) {

        Long Id = Long.valueOf(id);
        for (UserApp u : userRepository.findAll()) {
            if (u.getEmail().equals(email)) {
                ArrayList<Long> arr = u.getEventsSub();
                arr.add(Id);
                u.setEventsSub(arr);
                userRepository.save(u);
                //    System.out.println(arr.size());
            }
        }
    }


    //Добавить событию человека в subscribers
    @GetMapping(path = "/addUserToEvent")
    public @ResponseBody
    void addUserToEvent(@RequestParam String email, @RequestParam String id) {

        Long Id = Long.valueOf(id);
        EventApp u = eventRepository.findById(Id).get();

        ArrayList<String> arr = u.getPeople();
        for (UserApp user : userRepository.findAll())
            if (user.getEmail().equals(email)) {
                arr.add(Long.toString(user.getId()));
                u.setPeople(arr);
                eventRepository.save(u);
                return;
            }
    }


    @GetMapping(path = "/findEventById")
    public @ResponseBody
    Event findEventById(@RequestParam String id) {

        Long Id = Long.valueOf(id);
        Event result;
        EventApp evapp = eventRepository.findById(Id).get();
        result = new Event(id, evapp.getName(), evapp.getDecription(), evapp.getAuthor());
        result.setImage(evapp.getImage());
        result.setArrayThings(evapp.getArrayThings());
        result.setAddress(evapp.getAddress());
        result.setDate(evapp.getDate());
        result.setKind(evapp.getKind());
        result.setTime(evapp.getTime());
        result.setPosition(evapp.getPosition());
        return result;
    }


    @GetMapping(path = "/deleteById")
    public @ResponseBody
    void deleteById(@RequestParam String id) {
        System.out.println("start");
        Long Id = Long.valueOf(id);
        EventApp event = eventRepository.findById(Id).get();
        ArrayList<String> arr = event.getPeople();
        ArrayList<Long> idUsers = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            idUsers.add(Long.valueOf(arr.get(i)));
        }
        for (int i = 0; i < idUsers.size(); i++) {
            UserApp u = userRepository.findById(idUsers.get(i)).get();
            ArrayList<Long> arrEv = u.getEventsSub();
            ArrayList<Long> newList = new ArrayList<>();
            for (int j = 0; j < arrEv.size(); j++) {
                System.out.println(arrEv.get(j));
                if (!arrEv.get(j).equals(Id)) {
                    newList.add(arrEv.get(j));
                }

            }
            u.setEventsSub(newList);
            userRepository.save(u);
        }
        eventRepository.deleteById(Id);
    }


    @GetMapping(path = "/userByEmail")
    public @ResponseBody
    User userByEmail(@RequestParam String email) {
        User user = new User();
        for (UserApp u : userRepository.findAll()) {
            if (u.getEmail().equals(email)) {
                user.setImage(u.getImage());
                user.setId(Long.toString(u.getId()));
                user.setEmail(u.getEmail());
                user.setPassword(u.getPassword());
                user.setCity(u.getCity());
                user.setAge(u.getAge());
                user.setName(u.getmName());
                return user;
            }
        }
        return null;
    }


    @GetMapping(path = "/changeThingById")
    public @ResponseBody
    void changeThingById(@RequestParam String Id, @RequestParam Integer number) {
        //  Event event = new Event();
        EventApp evapp = eventRepository.findById(Long.valueOf(Id)).get();
        ArrayThings arrthings = evapp.getThings();
        ArrayList<Thing> arr = arrthings.getArr();
        if (arr.get(number).getValue())
            arr.get(number).setValue(false);
        else
            arr.get(number).setValue(true);
        arrthings.setArr(arr);
        evapp.setThings(arrthings);
        eventRepository.save(evapp);
        template.convertAndSend("/topic/greeting" + Id, "2");
    }

    @GetMapping(path = "/thingsOfEvent")
    public @ResponseBody
    ArrayThings thingsOfEvent(@RequestParam String Id) {
        //  Event event = new Event();
        EventApp evapp = eventRepository.findById(Long.valueOf(Id)).get();
        return evapp.getThings();
    }


    @GetMapping(path = "/addThings")
    public @ResponseBody
    void addThings(@RequestParam String Id, @RequestParam String name) {
        MessageApp message = new MessageApp("###");

        EventApp evapp = eventRepository.findById(Long.valueOf(Id)).get();
        ArrayThings arrthings = evapp.getThings();
        ArrayList<Thing> arr = arrthings.getArr();
        arr.add(new Thing(name));
        arrthings.setArr(arr);
        evapp.setThings(arrthings);
        eventRepository.save(evapp);
        template.convertAndSend("/topic/greeting" + Id, "2");
    }


    // события пользовтеля
    // TODO
    // хранение картинки
    // getInformationAboutEvents(String email) return EventApp;
    // Все сообщения по Id события
    // AddMessage

    //получение пользователя по почте
}