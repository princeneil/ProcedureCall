package  my.company.handlers.catalogservice; 

import com.sap.cloud.sdk.service.prov.api.DataSourceHandler;
import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.annotations.Action;
import com.sap.cloud.sdk.service.prov.api.exception.DatasourceException;
import com.sap.cloud.sdk.service.prov.api.request.OperationRequest;
import com.sap.cloud.sdk.service.prov.api.response.OperationResponse;
import com.sap.cloud.sdk.service.prov.api.ExtensionHelper;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.sql.Connection;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import com.sap.cloud.sdk.hana.connectivity.handler.CDSDataSourceHandler;
import com.sap.cloud.sdk.hana.connectivity.handler.DataSourceHandlerFactory;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSQuery;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryBuilder;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryResult;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSException;
import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.response.CreateResponse;


//import com.sap.cloud.sdk.service.prov.api.operations.Query;
//import com.sap.cloud.sdk.service.prov.api.request.QueryRequest;
//import com.sap.cloud.sdk.service.prov.api.response.QueryResponse;
//import com.sap.cloud.sdk.service.prov.api.operations.Read;
//import com.sap.cloud.sdk.service.prov.api.request.ReadRequest;
//import com.sap.cloud.sdk.service.prov.api.response.ReadResponse;
//import com.sap.cloud.sdk.service.prov.api.operations.Update;
//import com.sap.cloud.sdk.service.prov.api.request.UpdateRequest;
//import com.sap.cloud.sdk.service.prov.api.response.UpdateResponse;
//import com.sap.cloud.sdk.service.prov.api.operations.Create;
//import com.sap.cloud.sdk.service.prov.api.request.CreateRequest;
//import com.sap.cloud.sdk.service.prov.api.response.CreateResponse;
//import com.sap.cloud.sdk.service.prov.api.operations.Delete;
//import com.sap.cloud.sdk.service.prov.api.request.DeleteRequest;
//import com.sap.cloud.sdk.service.prov.api.response.DeleteResponse;
//import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Handler class for entity "Books" of service "CatalogService".
 * This handler registers custom handlers for the entity OData operations.
 * For more information, see: https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/6fe3070250ea45b88c35cda209e8324b.html
 */
public class BooksCustomHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Action(Name="callProc", serviceName="CatalogService")
	public OperationResponse procResponse( OperationRequest actionRequest, ExtensionHelper extensionHelper )
	{
		//EntityData ed = null;
		//CreateResponse createResponse;
		OperationResponse procResponse = null;
		
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(), "my.bookshop");
		
		CDSQuery cdsQuery = new CDSSelectQueryBuilder("CatalogService.Books")
            .top(2)
            .selectColumns("ID", "Title", "Stock")
            .build();                  			
		
		try {
			CDSSelectQueryResult cdsSelectQueryResult = dsHandler.executeQuery(cdsQuery);
			List<EntityData> ed = cdsSelectQueryResult.getResult();
			//createResponse = CreateResponse.setSuccess().setData(ed).response();
			procResponse = OperationResponse.setSuccess().setEntityData(ed).response();
			return procResponse;
			//ed = cdsSelectQueryResult.getResult();
			//ed = null;
		} catch (CDSException e) {
			ErrorResponse er = ErrorResponse.getBuilder().setMessage("BROKEN")
				.setStatusCode(500).setCause(e.getCause()).response();
			//createResponse = CreateResponse.setError(er);
			//return createResponse;
			return OperationResponse.setError(er);
		}
	    
	    //return createResponse;		
		//return null;
	}

	private static Connection getConnection() {
	  Connection conn = null;
	  Context ctx;
	  try {
	    ctx = new InitialContext();
	    conn = ((DataSource) ctx.lookup("java:comp/env/jdbc/java-hdi-container")).getConnection();
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  return conn;
	}

	@Action(Name="insertBook", serviceName="CatalogService")
	public OperationResponse updateStock(OperationRequest actionRequest, ExtensionHelper extensionHelper )
	{
		
		Map<String, Object> parameters = actionRequest.getParameters();
		DataSourceHandler handler = extensionHelper.getHandler();
		
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("ID", Integer.parseInt(parameters.get("bookId").toString()));

		try {
			//fetching the book details for the id and fetching the stock
//			EntityData entityData = handler.executeRead("Books", keys, actionRequest.getEntityMetadata().getFlattenedElementNames());
			List <String> properties = Arrays.asList("ID", "title", "stock");
			EntityData entityData = handler.executeRead("Books", keys, properties);
			
			//Apply multiplier to stock
			Integer stock = Integer.parseInt(entityData.getElementValue("stock").toString());
			stock = stock * Integer.parseInt(parameters.get("multiplier").toString());
			
			//reconstruct the entityData with new stock level
			entityData = entityData.getBuilder(entityData).removeElement("stock").addElement("stock", stock).buildEntityData("Books");
			//update the stock
			handler.executeUpdate(entityData, keys, false);
			
			OperationResponse response = OperationResponse.setSuccess().setEntityData(Arrays.asList(entityData)).response();
			return response;
			
		} catch (DatasourceException e) {
			logger.error("Error accessing the data", e);
			return null;
		}
	}



//	@Query(entity = "Books", serviceName = "CatalogService")
//	public QueryResponse queryBooks(QueryRequest req) {
//      //TODO: add your custom logic...
//
//      //List<Object> resultItems = new ArrayList<Object>();
//      //return QueryResponse.setSuccess().setData(resultItems).response(); //use this API to return items.
//      ErrorResponse errorResponse = ErrorResponse.getBuilder()
//        					.setMessage("Unimplemented Query Operation")
//        					.setStatusCode(500)
//        					.response();
//      return QueryResponse.setError(errorResponse);
//	}

//	@Read(entity = "Books", serviceName = "CatalogService")
//	public ReadResponse readBooks(ReadRequest req) {
//      //TODO: add your custom logic...
//
//      //Object data = new Object();
//      //return ReadResponse.setSuccess().setData(data).response(); //use this API to return an item.
//      ErrorResponse errorResponse = ErrorResponse.getBuilder()
//        					.setMessage("Unimplemented Read Operation")
//        					.setStatusCode(500)
//        					.response();
//      return ReadResponse.setError(errorResponse);
//	}

//	@Update(entity = "Books", serviceName = "CatalogService")
//	public UpdateResponse updateBooks(UpdateRequest req) {
//      //TODO: add your custom logic...
//
//      //return UpdateResponse.setSuccess().response(); //use this API if the item is successfully modified.
//      ErrorResponse errorResponse = ErrorResponse.getBuilder()
//        					.setMessage("Unimplemented Update Operation")
//        					.setStatusCode(500)
//        					.response();
//      return UpdateResponse.setError(errorResponse);
//	}

//	@Create(entity = "Books", serviceName = "CatalogService")
//	public CreateResponse createBooks(CreateRequest req) {
//      //TODO: add your custom logic...
//
//      //return CreateResponse.setSuccess().response(); //use this API if the item is successfully created.
//      ErrorResponse errorResponse = ErrorResponse.getBuilder()
//        					.setMessage("Unimplemented Create Operation")
//        					.setStatusCode(500)
//        					.response();
//      return CreateResponse.setError(errorResponse);
//	}

//	@Delete(entity = "Books", serviceName = "CatalogService")
//	public DeleteResponse deleteBooks(DeleteRequest req) {
//      //TODO: add your custom logic...
//
//      //return DeleteResponse.setSuccess().response(); //use this API if the item is successfully deleted.
//      ErrorResponse errorResponse = ErrorResponse.getBuilder()
//        					.setMessage("Unimplemented Delete Operation")
//        					.setStatusCode(500)
//        					.response();
//      return DeleteResponse.setError(errorResponse);
//	}

}