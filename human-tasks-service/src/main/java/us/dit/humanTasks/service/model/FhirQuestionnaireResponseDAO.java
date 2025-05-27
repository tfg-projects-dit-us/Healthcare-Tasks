/**
*  This file is part of Healthcare Tasks: Human task management in healthcare contexts.
*  Copyright (C) 2024  Universidad de Sevilla/Departamento de Ingeniería Telemática
*
*  Healthcare Tasks is free software: you can redistribute it and/or
*  modify it under the terms of the GNU General Public License as published
*  by the Free Software Foundation, either version 3 of the License, or (at
*  your option) any later version.
*
*  Healthcare Tasks is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
*  Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with Healthcare Tasks. If not, see <https://www.gnu.org/licenses/>.
**/
package us.dit.humanTasks.service.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.Quantity;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.DateType;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.TimeType;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.DecimalType;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.hl7.fhir.r5.model.QuestionnaireResponse.QuestionnaireResponseItemComponent;
import org.hl7.fhir.r5.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.UrlUtil;


/**
 * @author Juan Manuel Ostos Rabadán
 * @author Isabel Román
 * @version 19/12/2024
 * 
 * En esta versión se ha cambiado de nombre la clase, para ser coherente con el dao de tarea
 * Además se prepara que la tarea tenga identificado el cuestionario con la url completa o sólo con el id en el servidor base de la propia tarea
 */
@Service
public class FhirQuestionnaireResponseDAO {
	private static final Logger logger = LogManager.getLogger();
	@Autowired
	us.dit.humanTasks.service.model.FhirTasksDAO taskDao;

	/**
	 * Busca un cuestionario respuesta a partir de su url completa
	 * @param url completa del cuestionario
	 * @return el cuestionario
	 */
	public QuestionnaireResponse getQuestionnaireResponse(String url) {	
				FhirContext ctx = FhirContext.forR5();
				//Necesito sacar del url por un lado el servidor y por otro el id
				String serverBase;
				QuestionnaireResponse questionnaireResponse=null;
				try {
					logger.debug("Busco questionnaireResponse " + url);		
					serverBase=getServerBase(url);					
					String questionnaireResponseId=UrlUtil.parseUrl(url).getResourceId();	
					logger.debug("serverBase: "+serverBase);
					logger.debug("questionnaireResponse id "+questionnaireResponseId);
					IGenericClient client = ctx.newRestfulGenericClient(serverBase);			
					questionnaireResponse =
					      client.read().resource(QuestionnaireResponse.class).withId(questionnaireResponseId).execute();
					logger.info("Localizado questionnaireResponse " + questionnaireResponse.getId());
				} catch (Exception e) {
			
					logger.debug("Error al recuperar questionnaireResponse del servidor");
					e.printStackTrace();
				}
				return questionnaireResponse;		
	}

		/**
	 * Formatea los datos del item del cuestionario para mostrarlos en la plantilla
	 * @param url completa del cuestionario
	 * @return el cuestionario
	 */
	public HashMap<String, List<String>> getQuestionnaireResponseItems(QuestionnaireResponse questionnaireResponse) {	
			List<QuestionnaireResponseItemComponent> items = questionnaireResponse.getItem();
			HashMap<String, List<String>> FormattedItems = new LinkedHashMap<>();
			for (QuestionnaireResponseItemComponent item : items) {
				List<String> FormattedAnswer = new ArrayList<>();
				String text = null;
				if(item.hasText()){
					text = item.getText();	
				}
				if(item.hasAnswer()){
					List<QuestionnaireResponseItemAnswerComponent> answers = item.getAnswer();
					//List<String> FormattedAnswer = new ArrayList<>();
					for (QuestionnaireResponseItemAnswerComponent answer : answers) {
						if(answer.hasValueCoding()){
							Coding value = answer.getValueCoding();
							FormattedAnswer.add(value.getDisplay()+": "+value.getCode());
						}
						if(answer.hasValueDateTimeType()){
							DateTimeType value= answer.getValueDateTimeType();
							FormattedAnswer.add(value.toHumanDisplayLocalTimezone());
						}
						if(answer.hasValueDateType()){
							DateType value= answer.getValueDateType();
							FormattedAnswer.add(value.toHumanDisplayLocalTimezone());
						}
						if(answer.hasValueTimeType()){
							TimeType value= answer.getValueTimeType();
							FormattedAnswer.add(value.getValueAsString());
						}
						if(answer.hasValueDecimalType()){
							DecimalType value= answer.getValueDecimalType();
							FormattedAnswer.add(value.getValue().toString());							
						}
						if(answer.hasValueReference()){
							Reference value= answer.getValueReference();
							FormattedAnswer.add(value.getReference());
						}
						if(answer.hasValueIntegerType()){
							IntegerType value= answer.getValueIntegerType();
							FormattedAnswer.add(value.getValue().toString());							
						}	
						if(answer.hasValueQuantity()){
						Quantity value = answer.getValueQuantity();
							FormattedAnswer.add(value.getValue()+" "+value.getUnit());
						}	
						if(answer.hasValueUriType()){
							UriType value= answer.getValueUriType();
							FormattedAnswer.add(value.getValue());
						}	
						if(answer.hasValueStringType()){
							StringType value= answer.getValueStringType();
							FormattedAnswer.add(value.getValue());
						}		
						if(answer.hasValueBooleanType()){
							BooleanType value= answer.getValueBooleanType();
							FormattedAnswer.add(value.getValue()? "Sí":"No");
						}																																													
					}
				}
				FormattedItems.put(text,FormattedAnswer);
			}		
		return FormattedItems;
	}

	/**
	 * Busca el servidor base de la url del cuestionario que se pasa, si no es la url de un Questionnaire devuelve null
	 * @param url del questionnaire
	 * @return servidor base o null si la url no es de un Questionnaire
	 */
	public String getServerBase(String url) {
		
		String serverBase=null;
		logger.debug("Pregunto el servidor base del cuestionario con url ",url);		
		int pos=url.indexOf("Questionnaire");
		if (pos!=-1) {
			serverBase=url.substring(0,pos);
			logger.debug("Localizado servidor base "+ serverBase);
		}
		return serverBase;		
	}
}
