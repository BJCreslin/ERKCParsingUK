package model;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TSJ {
    private String name;
    private String adressNumber;
    private String email;

    public TSJ(String html, String val) {
        adressNumber = html;
        name = val;
    }
}
