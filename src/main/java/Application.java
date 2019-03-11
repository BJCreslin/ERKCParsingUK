import model.TSJ;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {
    /**
     *
     * @param args
     * @throws IOException
     * Программа для парсинга названий и электронной почты ТСЖ на сайте https://vc.tom.ru/pages/
     * и записи результатов в файл.
     */
    public static void main(String[] args) throws IOException {

        String siteAdress = "https://vc.tom.ru/pages/";

        List<TSJ> tsjList = new ArrayList<>();

        Document html = Jsoup.connect(siteAdress + "tsj/").get();
        // <select class="use-select2"
        Element useSelect2Element = html.getElementsByClass("use-select2").first();
/*
                                      *****
                                            <option value="422" >Жилищно-строительный кооператив "Авангард"</option>
                                            <option value="231" >Жилищно-строительный кооператив "Венера-2"</option>
                                            <option value="244" >Жилищно-строительный кооператив "Герцена, 43а"</option>
                                      ******
 */
        for (Element element : useSelect2Element.getElementsByTag("option")) {
            tsjList.add(new TSJ(element.html(), element.val()));
        }

        emailFinder(siteAdress, tsjList);
        saveXLS(tsjList);
        return;
    }

    /**
     * @param siteAdress
     * @param tsjList    List c данными ТСЖ . Нужен заполененный AdressNumber.
     *                   так как он в адресе страницы https://vc.tom.ru/pages/331/
     * @throws IOException Перебираем все ТСЖ из Списка. Заходим на страничку каждого-  siteAdress + tsj.getAdressNumber() + "/"
     *                     Ищем совпадения паттерна на емайл [a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+
     *                     Берём второй! ТАк как первый будет давать емайл разработчиков страницы 8))):
     *                     if (matcher.find()) {
     *                     if (matcher.find()) {
     *                     tsj.setEmail(matcher.group());
     *                     }
     *                     }
     */
    private static void emailFinder(String siteAdress, List<TSJ> tsjList) throws IOException {
        Pattern patternEmail = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        for (TSJ tsj : tsjList) {
            String adressTSJ = siteAdress + tsj.getAdressNumber() + "/";
            Document documentTSJ = Jsoup.connect(adressTSJ).userAgent("Mozilla").get();
            Matcher matcher = patternEmail.matcher(documentTSJ.body().html());
            if (matcher.find()) {
                if (matcher.find()) {
                    tsj.setEmail(matcher.group());
                }
            }
        }
    }


    /**
     * @param tsjList
     * @throws IOException Save date to xlsfile (  String fileName = "C:\\D\\xlsforsite\\тсж.xls";)
     *                     Если поле емайл пустое, то не записываем.
     *                     Название файла- String fileName = "C:\\D\\xlsforsite\\тсж.xls";
     */
    private static void saveXLS(List<TSJ> tsjList) throws IOException {
        String fileName = "C:\\D\\xlsforsite\\тсж.xls";
        Cell cellTemp;
        int rowNumber = 0;
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        Sheet sheet = hssfWorkbook.createSheet();
        for (TSJ tsj : tsjList) {
//            не у всех ТСЖ заполнены email. Поэтому проверка на то, что не пустой е-майл.
            if (!tsj.getEmail().isEmpty()) {
                Row row = sheet.createRow(rowNumber++);

                cellTemp = row.createCell(0);
                cellTemp.setCellValue(tsj.getName());

                cellTemp = row.createCell(1);
                cellTemp.setCellValue(tsj.getEmail());
            }
        }
        File file = new File(fileName);
        hssfWorkbook.write(file);
        hssfWorkbook.close();
    }
}
