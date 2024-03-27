package us.dit.consentimientos.service.controllers;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Attachment;
import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.DataType;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.DateType;
import org.hl7.fhir.r5.model.DecimalType;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.Quantity;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.hl7.fhir.r5.model.QuestionnaireResponse.QuestionnaireResponseItemComponent;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.TimeType;
import org.hl7.fhir.r5.model.UrlType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * @author Marco Antonio Maldonado Orozco
 */

@Controller
public class FormController {
	private static final Logger logger = LogManager.getLogger();

    @PostMapping("/submit")
    public String procesarFormulario(@RequestParam Map<String, String> responseByInputName, 
    		@RequestParam Map<String, MultipartFile> filesByInputName,
    		@RequestParam("questionnaireId") String questionnaireUrl, Model model) {
    	
    	//Obtengo el Questionnaire para poder buscar los items asociados a las respuestas a partir del linkId de cada item
    	Questionnaire questionnaire = getQuestionnaire(questionnaireUrl);
    	if (questionnaire == null) {
    		return "paginaDeError";
    	}
    	//Construyo el QuestionnaireResponse para guardar las respuestas del formulario
    	QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        questionnaireResponse.setQuestionnaire("Questionnaire/" + questionnaire.getId());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        //Recorro todas las respuestas de tipo String (Todas menos las de tipo attachment que son ficheros)
        for (Map.Entry<String, String> entry : responseByInputName.entrySet()) {	
            String inputName = entry.getKey();
            String value = entry.getValue();
            //Compruebo que el valor no sea nulo ni vacío para filtrar preguntas sin respuestas
        	if (value != null && !value.isEmpty()) {
        		//Esta separación es porque el nombre de todos los items son linkId~nombreDelInput, de esta forma se obtiene el linkId
        		//para buscar el cuestionario
        		String[] parts = inputName.split("~");
                String linkId = parts[0];
                //A partir del linkId obtengo el item del Questionnaire para saber el tipo y poder construir la respuesta adecuada
                QuestionnaireItemComponent questionnaireItem = getItemFromLinkId(linkId, questionnaire);
                if (questionnaireItem != null) {
                	//Compruebo si existe algún item con ese linkId en el questionnaireResponse, por si hay preguntas con respuestas múltilples
                	//guardar todas las respuestas en el mismo item y no crear un item distinto para la misma pregunta con las distintas respuestas
                	 QuestionnaireResponse.QuestionnaireResponseItemComponent responseItem = findResponseItemByLinkId(linkId, questionnaireResponse);
                     //En caso de que no exista creo un nuevo Item del QuestionnarieResponse y lo añado.
                	 if (responseItem == null) {
                         responseItem = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
                         responseItem.setLinkId(questionnaireItem.getLinkId());
                         questionnaireResponse.addItem(responseItem);
                     }
                	 //Según el tipo de pregunta construyo la respuesta pertinente y la añado al Item del QuestionnaireResponse
                     switch (questionnaireItem.getType()) {
	                     case BOOLEAN:
	                         responseItem.addAnswer().setValue(new BooleanType(Boolean.parseBoolean(value)));
	                         break;
	                     case DECIMAL:
	                         responseItem.addAnswer().setValue(new DecimalType(value));
	                         break;
	                     case INTEGER:
	                         responseItem.addAnswer().setValue(new IntegerType(Integer.parseInt(value)));
	                         break;
	                     case QUANTITY:
	                    	 Quantity quantity = new Quantity()
	                         	.setValue(new BigDecimal(value))
	                         	.setCode(questionnaireItem.getAnswerOptionFirstRep().getValueCoding().getCode())
	                         	.setUnit(questionnaireItem.getAnswerOptionFirstRep().getValueCoding().getCode());
	                    	 responseItem.addAnswer().setValue(quantity);
	                    	 break;
	                     case DATE:
	                    	 DateType date = createDateType(value);
	                    	 if(date != null) {
	                    		 responseItem.addAnswer().setValue(date);	                    		 
	                    	 }
	                         break;
	                     case DATETIME:
	                    	 DateTimeType dateTime = createDateTimeType(value);
	                    	 if(dateTime != null) {
	                    		 responseItem.addAnswer().setValue(dateTime);	                    		 
	                    	 }
	                         break;
	                     case TIME:
	                         responseItem.addAnswer().setValue(new TimeType(value));
	                         break;
	                     case STRING:
	                         responseItem.addAnswer().setValue(new StringType(value));
	                         break;
	                     case TEXT:
	                         responseItem.addAnswer().setValue(new StringType(value));
	                         break;
	                     case URL:
	                         responseItem.addAnswer().setValue(new UrlType(value));
	                         break;
	                     case REFERENCE:
	                    	 Reference reference = new Reference();
	                         reference.setReference(value);
	                         responseItem.addAnswer().setValue(reference);
	                         break;
	                     case CODING:
	                         Coding coding = new Coding();
	                         coding.setDisplay(value);
	                         responseItem.addAnswer().setValue(coding);
	                         break;
	                     default:
                    	 	System.out.println("Este tipo de item no está contemplado");
	                         break;
                     }
                } else {
                	logger.error("No se encuentra item en questionnaire con linkId: " + linkId);
                }
            }
        }
        
        //En este for se recorren los inputs de tipo attachment en caso de que haya, ya que en el otro mapa no vienen al ser values string
        //El funcionamiento es el mismo pero adaptado al Item de tipo attachment
        for (Map.Entry<String, MultipartFile> entry : filesByInputName.entrySet()) {	
    		String inputName = entry.getKey();
    		MultipartFile file = entry.getValue();
    		if (file != null && !file.isEmpty()) {
        		String[] parts = inputName.split("~");
                String linkId = parts[0];
                QuestionnaireItemComponent questionnaireItem = getItemFromLinkId(linkId, questionnaire);
                if (questionnaireItem != null && QuestionnaireItemType.ATTACHMENT.equals(questionnaireItem.getType())) {
                	QuestionnaireResponse.QuestionnaireResponseItemComponent responseItem = findResponseItemByLinkId(linkId, questionnaireResponse);
                	if (responseItem == null) {
	                    responseItem = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
	                    responseItem.setLinkId(questionnaireItem.getLinkId());
	                    questionnaireResponse.addItem(responseItem);
                	}
            		try {		
                		Attachment attachment = new Attachment();
                		attachment.setTitle(file.getOriginalFilename());
                        attachment.setContentType(file.getContentType());
						attachment.setData(file.getBytes());
						responseItem.addAnswer().setValue(attachment);
            		} catch (IOException e) {
						System.err.println("Error obteniendo los datos del fichero de la pregunta" + questionnaireItem.getLinkId() 
						+ "\n" + e.getStackTrace());
					}
                } else {
                	logger.error("No se encuentra item de tipo attachment en questionnaire con linkId: " + linkId);
                }
    		}
        }
        printQuestionnaireResponseItems(questionnaireResponse);
        return "paginaDeExito";
    }
    
    public void printQuestionnaireResponseItems(QuestionnaireResponse questionnaireResponse) {
        for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()) {
            String linkId = item.getLinkId();
            System.out.println("LinkId: " + linkId);
            for (QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer : item.getAnswer()) {
                String respuesta = getResponseValue(answer.getValue());
                System.out.println("   Respuesta: " + respuesta);
            }
        }
    }

    private String getResponseValue(DataType value) {
        if (value instanceof BooleanType) {
            return Boolean.toString(((BooleanType) value).getValue());
        } else if (value instanceof DecimalType) {
            return ((DecimalType) value).getValue().toString();
        } else if (value instanceof IntegerType) {
            return ((IntegerType) value).getValue().toString();
        } else if (value instanceof Quantity) {
            return ((Quantity) value).getValue().toString() + " " + ((Quantity) value).getUnit();
        } else if (value instanceof DateType) {
            return ((DateType) value).getValueAsString();
        } else if (value instanceof DateTimeType) {
            return ((DateTimeType) value).getValueAsString();
        } else if (value instanceof TimeType) {
            return ((TimeType) value).getValueAsString();
        } else if (value instanceof StringType) {
            return ((StringType) value).getValue();
        } else if (value instanceof UrlType) {
            return ((UrlType) value).getValue();
        } else if (value instanceof Reference) {
            return ((Reference) value).getReference();
        } else if (value instanceof Coding) {
            return ((Coding) value).getDisplay();
        } else if (value instanceof Attachment) {
            return ((Attachment) value).getTitle();
        } else {
            return "Tipo de respuesta no compatible";
        }
    }
    
    private QuestionnaireItemComponent getItemFromLinkId(String linkId, Questionnaire questionnaire) {
        if (questionnaire != null && questionnaire.hasItem()) {
            for (QuestionnaireItemComponent item : questionnaire.getItem()) {
            	QuestionnaireItemComponent foundItem = getItemFromLinkIdRecursive(item, linkId);
            	if (foundItem != null) {
                    return foundItem;
                }
            }
        }
        return null;
    }
    
    private QuestionnaireItemComponent getItemFromLinkIdRecursive(QuestionnaireItemComponent item, String linkId) {
        if (item.getLinkId().equals(linkId)) {
            return item;
        }
        //En caso de que no coincida puede ser porque la pregunta es de tipo question y el item del que tenemos la respuesta está
        //dentro, por eso comprobamos que pueda tener más items dentro
        if ((item.getType() == Questionnaire.QuestionnaireItemType.QUESTION ||
        		item.getType() == Questionnaire.QuestionnaireItemType.GROUP) && item.hasItem()) {
            for (QuestionnaireItemComponent subItem : item.getItem()) {
                QuestionnaireItemComponent foundItem = getItemFromLinkIdRecursive(subItem, linkId);
                if (foundItem != null) {
                    return foundItem;
                }
            }
        }
        return null;
    }
    
    public static DateType createDateType(String valor) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedDate = parseDate(valor, dateFormat);
        return parsedDate != null ? new DateType(parsedDate) : null;
    }

    public static DateTimeType createDateTimeType(String valor) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date parsedDate = parseDate(valor, dateFormat);
        return parsedDate != null ? new DateTimeType(parsedDate) : null;
    }

    private static Date parseDate(String valor, SimpleDateFormat dateFormat) {
        try {
            return dateFormat.parse(valor);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private QuestionnaireResponseItemComponent findResponseItemByLinkId(String linkId, QuestionnaireResponse questionnaireResponse) {
        for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()) {
            if (item.getLinkId().equals(linkId)) {
                return item;
            }
        }
        return null;
    }
    
    private Questionnaire getQuestionnaire(String questionnaireUrl) {
        // Encuentra la posición del primer "/" después de "Questionnaire/"
		// Ya que los URLs obtenidos de hapiFhir son así: https://hapi.fhir.org/baseR5/Questionnaire/677937/_history/1;
		// Para obtener solamente el ID:
    	int startIndex = questionnaireUrl.indexOf("/Questionnaire/") + "/Questionnaire/".length();
        int endIndex = questionnaireUrl.indexOf("/", startIndex);
        String questionnaireId = questionnaireUrl.substring(startIndex, endIndex);
        
    	FhirContext ctx = FhirContext.forR5();
    	String serverBase = "http://hapi.fhir.org/baseR5/";
		IGenericClient client = ctx.newRestfulGenericClient(serverBase);
		try {
	    	Questionnaire myQuestionnaire =
				      client.read().resource(Questionnaire.class).withId(questionnaireId).execute();
	    	return myQuestionnaire;
		} catch (Exception e) {
            logger.error("Se ha producido un error al obtener el cuestionario: " + questionnaireUrl +" : \n" + e.getMessage(), e);
            return null;
        }
    }
}