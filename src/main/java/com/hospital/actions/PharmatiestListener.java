package com.hospital.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hospital.entities.HospitalService;
import com.hospital.entities.PharmatiestDAO;
import com.hospital.models.*;
import com.hospital.utils.FormateDate;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;

public class PharmatiestListener {
	private HttpSession httpSession;
	private static PharmatiestListener pharmatiestListener = new PharmatiestListener();
	private static PharmatiestDAO pharmatiestDAO = HospitalService.getInstance();

	private PharmatiestListener() {}

	public static PharmatiestListener getInstance() {
		return pharmatiestListener;
	}

	private void handleError(HttpServletResponse response, String page, Exception ex) {
		try {
			response.sendRedirect(page + "?error=true");
		} catch (IOException e) {
			e.printStackTrace();
		}
		ex.printStackTrace();
	}

	private void updateDrugQuantityAndRedirect(HttpServletResponse response, Drug drug, int quantity, String successAction) throws IOException {
		int updatedQuantity = drug.getQuantity() - quantity;
		drug.setQuantity(updatedQuantity);
		pharmatiestDAO.updateObject(drug);
		if (updatedQuantity <= 50) {
			response.sendRedirect("selldrug.jsp?less50=true");
		} else {
			response.sendRedirect("selldrug.jsp?" + successAction + "=true");
		}
	}

	public void addDrug(HttpServletRequest request, HttpServletResponse response) {
		try {
			httpSession = request.getSession();
			Employee employee = (Employee) httpSession.getAttribute("employee");
			Drug drug = new Drug();
			drug.setName(request.getParameter("name"));
			drug.setCost(Double.parseDouble(request.getParameter("cost")));
			drug.setDrug_expired(FormateDate.getFormatedDate(request.getParameter("drug_expired")));
			drug.setStartDate(FormateDate.getFormatedDate(request.getParameter("startDate")));
			drug.setQuantity(Integer.parseInt(request.getParameter("quantity")));
			drug.setEmployee(employee);
			pharmatiestDAO.saveObject(drug);
			response.sendRedirect("drugmgmt.jsp?add=true");
		} catch (Exception ex) {
			handleError(response, "drugmgmt.jsp", ex);
		}
	}

	public void updateDrug(HttpServletRequest request, HttpServletResponse response) {
		try {
			int drugID = Integer.parseInt(request.getParameter("drugID"));
			httpSession = request.getSession();
			Employee employee = (Employee) httpSession.getAttribute("employee");
			Drug drug = (Drug) pharmatiestDAO.getObject(drugID, Drug.class);
			drug.setName(request.getParameter("name"));
			drug.setCost(Double.parseDouble(request.getParameter("cost")));
			drug.setDrug_expired(FormateDate.getFormatedDate(request.getParameter("drug_expired")));
			drug.setStartDate(FormateDate.getFormatedDate(request.getParameter("startDate")));
			drug.setQuantity(Integer.parseInt(request.getParameter("quantity")));
			drug.setEmployee(employee);
			pharmatiestDAO.updateObject(drug);
			response.sendRedirect("drugmgmt.jsp?update=true");
		} catch (Exception ex) {
			handleError(response, "drugmgmt.jsp", ex);
		}
	}

	public void deleteDrug(HttpServletRequest request, HttpServletResponse response) {
		try {
			int drugID = Integer.parseInt(request.getParameter("dempid"));
			Drug drug = (Drug) pharmatiestDAO.getObject(drugID, Drug.class);
			drug.setEmployee(null);
			pharmatiestDAO.deleteObject(drug);
			response.sendRedirect("drugmgmt.jsp?delete=true");
		} catch (Exception ex) {
			handleError(response, "drugmgmt.jsp", ex);
		}
	}

	public void sellDrug(HttpServletRequest request, HttpServletResponse response) {
		try {
			httpSession = request.getSession();
			Pharmatiest employee = (Pharmatiest) httpSession.getAttribute("employee");
			int quantity = Integer.parseInt(request.getParameter("quantity"));
			int drugID = Integer.parseInt(request.getParameter("drugID"));
			Drug drug = (Drug) pharmatiestDAO.getObject(drugID, Drug.class);
			int patientID = Integer.parseInt(request.getParameter("patientID"));
			Patient patient = (Patient) pharmatiestDAO.getObject(patientID, Patient.class);

			SelledDrug selledDrug = new SelledDrug();
			selledDrug.setSelledDrugID(pharmatiestDAO.getMaxIDObject());
			selledDrug.setPatient(patient);
			selledDrug.setPharmatiest(employee);
			selledDrug.setQuantity(quantity);
			selledDrug.setDrug(drug);
			selledDrug.setSelledDate(new Date());
			selledDrug.setUnitPerDay(request.getParameter("unitPerDay"));
			selledDrug.setEndDate(FormateDate.getFormatedDate(request.getParameter("endTake")));
			selledDrug.setStartDate(FormateDate.getFormatedDate(request.getParameter("startTake")));

			if (quantity < drug.getQuantity()) {
				pharmatiestDAO.sellDrug(selledDrug);
				updateDrugQuantityAndRedirect(response, drug, quantity, "add");
			} else {
				response.sendRedirect("selldrug.jsp?more=true");
			}
		} catch (Exception ex) {
			handleError(response, "selldrug.jsp", ex);
		}
	}

	public void updateSellDrug(HttpServletRequest request, HttpServletResponse response) {
		try {
			httpSession = request.getSession();
			int selledDrugID = Integer.parseInt(request.getParameter("selledDrugID"));
			int quantity = Integer.parseInt(request.getParameter("quantity"));
			int drugID = Integer.parseInt(request.getParameter("drugID"));
			Drug drug = (Drug) pharmatiestDAO.getObject(drugID, Drug.class);
			int patientID = Integer.parseInt(request.getParameter("patientID"));
			Patient patient = (Patient) pharmatiestDAO.getObject(patientID, Patient.class);

			SelledDrug selledDrug = new SelledDrug();
			selledDrug.setSelledDrugID(selledDrugID);
			selledDrug.setPatient(patient);
			selledDrug.setQuantity(quantity);
			selledDrug.setDrug(drug);
			selledDrug.setSelledDate(new Date());
			selledDrug.setUnitPerDay(request.getParameter("unitPerDay"));
			selledDrug.setEndDate(FormateDate.getFormatedDate(request.getParameter("endTake")));
			selledDrug.setStartDate(FormateDate.getFormatedDate(request.getParameter("startTake")));

			if (quantity < drug.getQuantity()) {
				pharmatiestDAO.updateObject(selledDrug);
				updateDrugQuantityAndRedirect(response, drug, quantity, "update");
			} else {
				response.sendRedirect("selldrug.jsp?more=true");
			}
		} catch (Exception ex) {
			handleError(response, "selldrug.jsp", ex);
		}
	}

	public void deleteSellDrug(HttpServletRequest request, HttpServletResponse response) {
		try {
			int selledDrugID = Integer.parseInt(request.getParameter("usdrug"));
			SelledDrug selledDrug = (SelledDrug) pharmatiestDAO.getObject(selledDrugID, SelledDrug.class);
			selledDrug.setDrug(null);
			selledDrug.setPatient(null);
			selledDrug.setPharmatiest(null);
			pharmatiestDAO.deleteObject(selledDrug);
			response.sendRedirect("selldrug.jsp?delete=true");
		} catch (Exception ex) {
			handleError(response, "selldrug.jsp", ex);
		}
	}

	public void getDrugInvoice(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int drugID = Integer.parseInt(request.getParameter("usdrug"));
		SelledDrug selledDrug = (SelledDrug) pharmatiestDAO.getObject(drugID, SelledDrug.class);
		ServletOutputStream servletOutputStream = response.getOutputStream();
		String imagePath = request.getServletContext().getRealPath("/reports/invoice_logo.png");
		InputStream reportStream = request.getServletContext().getResourceAsStream("/reports/Invoice.jrxml");
		try {
			JRDataSource dataSource = createReportDataSource(selledDrug);
			JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
			HashMap<String, Object> map = new HashMap<>();
			map.put("IMAGE_PATH", imagePath);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, dataSource);
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			response.setContentType("application/pdf");
			servletOutputStream.flush();
			servletOutputStream.close();
		} catch (Exception e) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace();
			response.setContentType("text/plain");
			response.getOutputStream().print(stringWriter.toString());
		}
	}

	private JRDataSource createReportDataSource(SelledDrug selledDrug) {
		Object[] reportRows = initializeBeanArray(selledDrug);
		return new JRBeanArrayDataSource(reportRows);
	}

	private Object[] initializeBeanArray(SelledDrug selledDrug) {
		List<SelledDrug> allSelledDrugs = pharmatiestDAO.getAllSelledDrugForPatient(selledDrug.getPatient().getPatientId());
		PatientDrugInvoice[] invoices = new PatientDrugInvoice[allSelledDrugs.size()];
		double total = 0;
		for (int i = 0; i < allSelledDrugs.size(); i++) {
			SelledDrug sd = allSelledDrugs.get(i);
			PatientDrugInvoice p = new PatientDrugInvoice();
			Patient patient = sd.getPatient();
			Drug drug = sd.getDrug();
			p.setAddress(patient.getAddress());
			p.setBloodGroupName(patient.getBloodGroup().getBloodGroupName());
			p.setEmail(patient.getEmail());
			p.setFamilyName(patient.getFamilyName());
			p.setFatherName(patient.getFatherName());
			p.setGender(patient.getGender());
			p.setJoiningDate(patient.getJoiningDate());
			p.setPhone(patient.getPhone());
			p.setDob(patient.getDob());
			p.setName(patient.getName());
			p.setPatientID(patient.getPatientId());
			p.setCost(drug.getCost());
			p.setDrugId(drug.getDrugId());
			p.setDrugName(drug.getName());
			p.setDrug_expired(sd.getEndDate());
			p.setStartDate(sd.getStartDate());
			p.setQuantity(sd.getQuantity());
			p.setUnitPerDay(sd.getUnitPerDay());
			total += drug.getCost() * sd.getQuantity();
			p.setTotalCost(total);
			invoices[i] = p;
		}
		for (PatientDrugInvoice invoice : invoices) {
			invoice.setTotalCost(total);
		}
		for (SelledDrug sd : allSelledDrugs) {
			sd.setDrug(null);
			sd.setPatient(null);
			sd.setPharmatiest(null);
			pharmatiestDAO.deleteObject(sd);
		}
		return invoices;
	}
}
