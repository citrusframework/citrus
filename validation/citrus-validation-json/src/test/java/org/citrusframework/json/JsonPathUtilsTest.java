package org.citrusframework.json;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPathUtilsTest {

    @Test
    public void testExtractNullValue() {
        String json = """
            {
                "animal": {
                    "name": "Garfield",
                    "type": "Cat",
                    "owner": null
                }
            }
            """;

        String name = JsonPathUtils.evaluateAsString(json, "$.animal.name");
        assertThat(name).isEqualTo("Garfield");
        String owner = JsonPathUtils.evaluateAsString(json, "$.animal.owner");
        assertThat(owner).isNull();

    }

    @Test
    public void testExtractNullValue2() {
        String json = """
            {
               "person": {
                 "inLiquidation": false,
                 "kurzbezeichnungIstFreitext": true,
                 "segmentNachKundenwertigkeit": null,
                 "sperrgrund": null,
                 "formularKBildId": null,
                 "istGesperrtBis": null,
                 "sitzgesellschaftDatum": null,
                 "sperrgrundBemerkung": null,
                 "anzahlMitarbeiterQuelle": "KUNDE",
                 "mwstCodeManuell": false,
                 "meldungBund": false,
                 "betreuungsegment": "GK_1N",
                 "bezeichnung": "NEC - Nippon Electric Company",
                 "betriebsgroesse": "GU",
                 "holdingNummer": null,
                 "sprache": "DE",
                 "personTyp": "RECHTSGEMEINSCHAFT",
                 "kontrollinhaberAngaben": "IN_ABKLAERUNG",
                 "mwstNummer": null,
                 "istInteressentVon": null,
                 "kurzbezeichnung": "20250530NEC - Nippon Electric",
                 "istGesperrt": false,
                 "risikobrancheZuteilung": null,
                 "potentialscore": null,
                 "risikobrancheDatum": "2025-05-30",
                 "mwstCode": "Z",
                 "lifeCycleStatus": "AKTIV",
                 "jahresumsatz": null,
                 "status": "AKTIV",
                 "statusDatum": "2025-05-10",
                 "erfassungsDatum": "2025-05-10",
                 "sitzgesellschaft": false,
                 "letzterKontakt": null,
                 "rechtsform": "AG",
                 "gdoVersion": "20250530220211212",
                 "angebotFuer": null,
                 "teledatakey": null,
                 "unternehmenID": null,
                 "istInteressent": false,
                 "lifeCycleStatusDatum": "2025-05-30T22:02:11.200",
                 "formularSBildId": null,
                 "istHolding": false,
                 "anzahlMitarbeiterDatum": "2025-05-30",
                 "personOID": "9000000000000044028",
                 "risikobranche": "KEINE_RISIKOBRANCHE_AUTOM_ERM",
                 "istGesperrtVon": null,
                 "wirtschaftlicherZweck": "IRRELEVANT",
                 "personNummer": 1000411954,
                 "rentabilitaet": null,
                 "istInteressentBis": null,
                 "anzahlMitarbeiter": 5330946,
                 "branchencode": "949200",
                 "kontrollinhaberAngabenGrund": "LEER"
               },
               "adresse": {
                 "ort": "Berikon",
                 "strasse": "Im Unterzelg",
                 "hausnummer": "777",
                 "land": "CH",
                 "plz": "8965"
               },
               "hinweiseResponse": []
             }
            """;

        Object name = JsonPathUtils.evaluate(json, "$.person.teledatakey");
        assertThat(name).isNull();

    }
}
