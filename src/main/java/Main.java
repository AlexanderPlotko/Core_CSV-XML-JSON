import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

//import static jdk.jfr.internal.jfc.JFCParser.parseXML;

public class Main {
    public static void main(String[] args) {

        String[] employeeUSA = "1,John,Smith,USA,25".split(","); //создали объект с полями
        String[] employeeRUS = "2,Inav,Petrov,RU,23".split(","); //создали объект с полями
        try (CSVWriter writer = new CSVWriter(new FileWriter("data.csv"))) {
            writer.writeNext(employeeUSA); //добавляем поля в файл "data.csv"
            writer.writeNext(employeeRUS); //добавляем поля в файл "data.csv"
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"}; //информация о колонках
        String fileName = "data.csv"; //имя считываемого файла

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); //инициализация документа(иерархия)
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element staff = document.createElement("staff");
            document.appendChild(staff);
            Element employee = document.createElement("employee");
            staff.appendChild(employee);
            Element id = document.createElement("id");
            id.appendChild(document.createTextNode("1"));
            employee.appendChild(id);
            Element firstName = document.createElement("firstName");
            firstName.appendChild(document.createTextNode("John"));
            employee.appendChild(firstName);
            Element lastName = document.createElement("lastName");
            lastName.appendChild(document.createTextNode("Smith"));
            employee.appendChild(lastName);
            Element country = document.createElement("country");
            country.appendChild(document.createTextNode("USA"));
            employee.appendChild(country);
            Element age = document.createElement("age");
            age.appendChild(document.createTextNode("25"));
            employee.appendChild(age);
            Element employee2 = document.createElement("employee");
            staff.appendChild(employee2);
            Element id2 = document.createElement("id");
            id2.appendChild(document.createTextNode("2"));
            employee2.appendChild(id2);
            Element firstName2 = document.createElement("firstName");
            firstName2.appendChild(document.createTextNode("Inav"));
            employee2.appendChild(firstName2);
            Element lastName2 = document.createElement("lastName");
            lastName2.appendChild(document.createTextNode("Petrov"));
            employee2.appendChild(lastName2);
            Element country2 = document.createElement("country");
            country2.appendChild(document.createTextNode("RU"));
            employee2.appendChild(country2);
            Element age2 = document.createElement("age");
            age2.appendChild(document.createTextNode("23"));
            employee2.appendChild(age2);

            DOMSource domSource = new DOMSource(document); //создание документа
            StreamResult strimResult = new StreamResult(new File("data.xml"));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(domSource, strimResult);

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

        List<Employee> list = parseCVR(columnMapping, fileName); //CVR
       // list.forEach(System.out::println);
        List<Employee> list1 = parseXML("data.xml"); //XML
      //  list1.forEach(System.out::println);

        String json = listToJson(list);
        String json1 = listToJson(list1);
        writeString(json);
        writeString(json1);
    }

    private static List<Employee> parseCVR(String[] columnMapping, String fileName) {
        List<Employee> list = null;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class); //указываем куда обращаться, в какой класс
            strategy.setColumnMapping(columnMapping); //указываем инфу о колонках (перечисление строк)
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader) //инструмент выбора стратегии
                    .withMappingStrategy(strategy)
                    .build();
            list = csv.parse(); //в лист объектов парсим CSV файл
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private static List<Employee> parseXML(String fileXML) {
        List<String> element = new ArrayList<>();
        List<Employee> list = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(fileXML));

            Node root = document.getDocumentElement(); //корневой каталог
            NodeList nodeList = root.getChildNodes(); //получаем список узлов
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i); //получаем список элементов
                if (node.getNodeName().equals("employee")) {
                    NodeList nodeList2 = node.getChildNodes();
                    for (int j = 0; j < nodeList2.getLength(); j++) {
                        Node node1 = nodeList2.item(j);
                        if (node1.getNodeType() == Node.ELEMENT_NODE) {
                            element.add(node1.getTextContent());
                        }
                        //System.out.println(node1.getNodeName() + ":" + node1.getTextContent());
                    }
                    list.add(new Employee(
                            Long.parseLong(element.get(0)),
                            element.get(1),
                            element.get(2),
                            element.get(3),
                            Integer.parseInt(element.get(4))));
                    element.clear();
                }
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        String json = gson.toJson(list, listType);
        System.out.println(gson.toJson(list, listType));
        System.out.println(gson.toJson(list));

        return json;
    }

    private static void writeString(String json) {
        try (FileWriter file = new FileWriter("data.json", true)) { //добавил true чтобы записался CSV и XML
            file.write(json);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
