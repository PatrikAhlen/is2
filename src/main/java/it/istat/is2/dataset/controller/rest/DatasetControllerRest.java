/**
 * Copyright 2019 ISTAT
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 *
 * @author Francesco Amato <framato @ istat.it>
 * @author Mauro Bruno <mbruno @ istat.it>
 * @author Paolo Francescangeli  <pafrance @ istat.it>
 * @author Renzo Iannacone <iannacone @ istat.it>
 * @author Stefano Macone <macone @ istat.it>
 * @version 1.0
 */
package it.istat.is2.dataset.controller.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import it.istat.is2.app.service.NotificationService;
import it.istat.is2.app.util.Utility;
import it.istat.is2.dataset.domain.DatasetColumn;
import it.istat.is2.dataset.domain.StatisticalVariableCls;
import it.istat.is2.dataset.service.DatasetService;

@RestController
public class DatasetControllerRest {

	@Autowired
	private DatasetService datasetService;
	@Autowired
	private NotificationService notificationService;

	@GetMapping("/datasetcolonnasql/{dfile}/{rigainf}/{rigasup}")
	@ResponseBody
	public ResponseEntity<?> loadDataSetColonnaSql(@PathVariable("dfile") Long dfile,
			@PathVariable("rigainf") Integer rigainf, @PathVariable("rigasup") Integer rigasup) throws IOException {
		List<DatasetColumn> df = datasetService.findAllDatasetColumnSQL(dfile, rigainf, rigasup);
		return ResponseEntity.ok(df);
	}

	@RequestMapping(value = "/rest/datasetvalori/{dfile}/{parametri:.+}", method = RequestMethod.POST)
	public ResponseEntity<?> loadDatasetValori(HttpServletRequest request, Model model,
			@PathVariable("dfile") Long dfile, @PathVariable("parametri") String parametri,
			@RequestParam("length") Integer length, @RequestParam("start") Integer start,
			@RequestParam("draw") Integer draw, @RequestParam Map<String, String> allParams)
			throws IOException, JSONException {

		String indexColunmToOrder = allParams.get("order[0][column]");
		String nameColumnToOrder = allParams.get("columns[" + indexColunmToOrder + "][data]");
		String dirColumnOrder = allParams.get("order[0][dir]");

		HashMap<String, String> parameters = null;
		String noparams = "noparams";
		if (!noparams.equals(parametri)) {
			StringTokenizer st = new StringTokenizer(parametri, "&");
			StringTokenizer st2 = null;
			parameters = new HashMap<String, String>();

			ArrayList<String> nomeValore = null;
			while (st.hasMoreTokens()) {
				st2 = new StringTokenizer(st.nextToken(), "=");
				nomeValore = new ArrayList<String>();
				while (st2.hasMoreTokens()) {
					nomeValore.add(st2.nextToken());
				}

				parameters.put(nomeValore.get(0), nomeValore.get(1));
			}
		}
		String dtb = datasetService.loadDatasetValori(dfile, length, start, draw, parameters, nameColumnToOrder,
				dirColumnOrder);

		return ResponseEntity.ok(dtb);
	}

	@RequestMapping(value = "/rest/setvariabilesum/{idcol}/{idvar}", method = RequestMethod.POST)
	public ResponseEntity<?> setVarSum(HttpServletRequest request, Model model, @PathVariable("idcol") Long idcol,
			@PathVariable("idvar") Integer idvar) throws IOException {

		DatasetColumn dcol = datasetService.findOneColonna(idcol);
		StatisticalVariableCls sum = new StatisticalVariableCls(idvar);
		dcol.setVariabileType(sum);
		try {
			dcol = datasetService.salvaColonna(dcol);
		} catch (Exception e) {
			notificationService.addErrorMessage("Errore: ", e.getMessage());
		}
		return ResponseEntity.ok(dcol);
	}

	@RequestMapping(value = "/rest/download/dataset/{tipoFile}/{dfile}", method = RequestMethod.GET)
	public void downloadWorkset(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("tipoFile") String tipoFile, @PathVariable("dfile") Long dfile) throws Exception {

		String fileName = "";
		String contentType = "";
		switch (tipoFile) {
		case "csv":
			fileName = "dataset.csv";
			contentType = "text/csv";
			break;
		case "pdf":
			fileName = "dataset.pdf";
			contentType = "application/pdf";
			break;
		case "excel":
			fileName = "dataset.xlsx";
			contentType = "application/vnd.ms-excel";
			break;
		}

		response.setHeader("charset", "utf-8");
		response.setHeader("Content-Type", contentType);
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		Map<String, List<String>> dataMap = datasetService.loadDatasetValori(dfile);
		Utility.writeObjectToCSV(response.getWriter(), dataMap);
	}

	@RequestMapping(value = "/rest/dataset/updaterowlist", method = RequestMethod.POST)
	public ResponseEntity<?> updateOrdineRighe(HttpServletRequest request, Model model,
			@RequestParam("ordineIds") String ordineIds) throws Exception {

		StringTokenizer stringTokenizerElements = new StringTokenizer(ordineIds, "|");
		String element = null;
		String ordine = null;
		String idcol = null;
		DatasetColumn datasetCol = new DatasetColumn();
		while (stringTokenizerElements.hasMoreElements()) {
			element = stringTokenizerElements.nextElement().toString();
			StringTokenizer stringTokenizerValues = new StringTokenizer(element, "=");
			while (stringTokenizerValues.hasMoreElements()) {
				ordine = stringTokenizerValues.nextElement().toString();
				idcol = stringTokenizerValues.nextElement().toString();
			}
			Long idc = Long.parseLong(idcol);
			Short ordineC = Short.parseShort(ordine);
			datasetCol = datasetService.findOneColonna(idc);

			datasetCol.setOrderCode(ordineC);
			datasetService.salvaColonna(datasetCol);
		}

		return ResponseEntity.ok("success");
	}

	@RequestMapping(value = "/rest/dataset/tables/{db}", method = RequestMethod.GET)
	public ResponseEntity<?> getTablesDB(HttpServletRequest request, Model model, @PathVariable("db") String db)
			throws IOException {
		return ResponseEntity.ok(datasetService.findTablesDB(db));
	}

	@RequestMapping(value = "/rest/dataset/fields/{db}/{table}", method = RequestMethod.GET)
	public ResponseEntity<?> getFieldsTableDB(HttpServletRequest request, Model model, @PathVariable("db") String db,
			@PathVariable("table") String table) throws IOException {
		return ResponseEntity.ok(datasetService.findFieldsTableDB(db, table));
	}
}
