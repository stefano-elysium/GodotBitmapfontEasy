package com.dashur.integration.extw.connectors.relaxgaming;

import com.dashur.integration.commons.RequestContext;
import com.dashur.integration.commons.exception.ApplicationException;
import com.dashur.integration.commons.exception.ValidationException;
import com.dashur.integration.commons.utils.CommonUtils;
import com.dashur.integration.extw.Constant;
import com.dashur.integration.extw.ExtwIntegConfiguration;
import com.dashur.integration.extw.Service;
import com.dashur.integration.extw.connectors.ConnectorServiceLocator;
import com.dashur.integration.extw.connectors.relaxgaming.data.service.Credentials;
import com.dashur.integration.extw.connectors.relaxgaming.data.service.ServiceRequest;
import com.dashur.integration.extw.connectors.relaxgaming.data.service.GetGamesResponse;
import com.dashur.integration.extw.connectors.relaxgaming.data.service.GetReplayRequest;
import com.dashur.integration.extw.connectors.relaxgaming.data.service.GetReplayResponse;
import com.dashur.integration.extw.connectors.relaxgaming.data.service.GetStateRequest;
import com.dashur.integration.extw.connectors.relaxgaming.data.service.GetStateResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.spi.HttpRequest;

@Slf4j
@Path("/v1/extw/exp/relaxgaming")
public class RelaxGamingController {
  static final String OPERATOR_CODE = Constant.OPERATOR_RELAXGAMING;
  static final String AUTHORIZATION = "Authorization";

  @Inject ExtwIntegConfiguration config;

  @Inject ConnectorServiceLocator connectorLocator;

  @Inject Service service;

  @Context HttpRequest request;

  private RelaxGamingConfiguration relaxConfig;

  @PostConstruct
  public void init() {
    relaxConfig = config.configuration(OPERATOR_CODE, RelaxGamingConfiguration.class);
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/version")
  public String version() {
    return config.getVersion();
  }

  /**
   * RelaxGaming launch game url
   *
   * @param gameid
   * @param ticket
   * @param jurisdiction
   * @param lang
   * @param channel
   * @param partnerid
   * @param moneymode
   * @param currency
   * @param clientid
   * @param homeurl
   * @param hidehome
   * @param fullscreen
   * @param plurl
   * @param rg_account_uri
   * @param accountlabel
   * @param sessiontimer
   * @param sessionresult
   * @param sessiontimelimit
   * @param sessionlapsed
   * @param sessionrcinterval
   * @param sessionwagered
   * @param sessionwon
   * @param sessionlostlimit
   * @param sessiontimewarninglimit
   * @param sessionlosswarninglimit
   * @param sessionshowsummary
   * @param sessiontimer
   * @param sessionresult
   * @return
   */
  @GET
  @Path("/launch")
  public Response getLauncher(
      @QueryParam("gameid") String gameId,
      @QueryParam("ticket") String token,
      @QueryParam("lang") String language,
      @QueryParam("partnerId") String partnerId,
      @QueryParam("moneymode") String mode,
      @QueryParam("currency") String demoCurrency,
      @QueryParam("channel") String platform,
      @QueryParam("homeurl") @DefaultValue("") String lobbyUrl) {
    try {
      if (log.isDebugEnabled()) {
        log.debug(
            "/v1/extw/exp/relaxgaming/launch - [{}] [{}] [{}] [{}] [{}] [{}]",
            gameId,
            language,
            partnerId,
            mode,
            demoCurrency,
            platform,
            lobbyUrl);
      }

      return getLauncherInternal(gameId, token, language, partnerId, mode, demoCurrency, lobbyUrl);
    } catch (Exception e) {
      log.error("Unable to launch game [{}] - [{}]", gameId, partnerId, e);
      return Response.serverError()
          .entity(
              String.format(
                  "<html><header><title>%s</title></header><body><p>%s</p></body></html>",
                  CommonUtils.getI18nMessages("msg.launch.error.title", getLocale(language)),
                  CommonUtils.getI18nMessages("msg.launch.error.description", getLocale(language))))
          .build();
    }
  }

  @POST
  @Path("/games/getgames")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getGames(
    @HeaderParam(AUTHORIZATION) String auth, final ServiceRequest request) {
    if (!authenticate(auth, request.getCredentials().getPartnerId())) {
      return Response.status(401).build();
    }
    if (log.isDebugEnabled()) {
      log.debug(
          "/v1/extw/exp/relaxgaming/games/getgames - [{}] [{}]",
          request.getCredentials(),
          request.getJurisdiction());
    }

    String partnerId = String.valueOf(request.getCredentials().getPartnerId());
    RelaxGamingConfiguration.CompanySetting setting = getCompanySettings(partnerId, false);

    GetGamesResponse resp = new GetGamesResponse();
    return Response.ok().type(MediaType.APPLICATION_JSON).encoding("utf-8").entity(resp).build();
  }

  @POST
  @Path("/round/getstate")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getState(
    @HeaderParam(AUTHORIZATION) String auth, final GetStateRequest request) {
    if (!authenticate(auth, request.getCredentials().getPartnerId())) {
      return Response.status(401).build();
    }
    if (log.isDebugEnabled()) {
      log.debug(
          "/v1/extw/exp/relaxgaming/state/getstate - [{}] [{}] [{}]",
          request.getCredentials(),
          request.getRoundId(),
          request.getJurisdiction());
    }

    String partnerId = String.valueOf(request.getCredentials().getPartnerId());
    RelaxGamingConfiguration.CompanySetting setting = getCompanySettings(partnerId, false);

    GetStateResponse resp = new GetStateResponse();
    return Response.ok().type(MediaType.APPLICATION_JSON).encoding("utf-8").entity(resp).build();
  }


  @POST
  @Path("/replay/get")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getPlaycheck(
    @HeaderParam(AUTHORIZATION) String auth, final GetReplayRequest request) {
    try {
      if (!authenticate(auth, request.getCredentials().getPartnerId())) {
        return Response.status(401).build();
      }
      if (log.isDebugEnabled()) {
        log.debug(
            "/v1/extw/exp/relaxgaming/game-state - [{}] [{}]",
            request.getCredentials().getPartnerId(),
            request.getRoundId());
      }

      String partnerId = String.valueOf(request.getCredentials().getPartnerId());
      RelaxGamingConfiguration.CompanySetting setting = getCompanySettings(partnerId, true);

      String url =
          service.playcheckUrl(
              RequestContext.instance(),
              setting.getLauncherAppClientId(),
              setting.getLauncherAppClientCredential(),
              setting.getLauncherAppApiId(),
              setting.getLauncherAppApiCredential(),
              request.getRoundId());

      GetReplayResponse resp = new GetReplayResponse();
      resp.setReplayUrl(url);
      return Response.ok().type(MediaType.APPLICATION_JSON).encoding("utf-8").entity(resp).build();

    } catch (Exception e) {
      log.error("Unable to get playcheck [{}] - [{}]", 
        request.getCredentials().getPartnerId(), request.getRoundId(), e);
        return Response.status(500).build();
    }
  }

  /**
   * Internal method for launching game
   *
   * @param gameId
   * @param token
   * @param language
   * @param partnerId
   * @param mode
   * @param demoCurrency
   * @param lobbyUrl
   * @return
   */
  private Response getLauncherInternal(
      String gameId,
      String token,
      String language,
      String partnerId,
      String mode,
      String demoCurrency,
      String lobbyUrl) {
    if (!partnerId.equals(relaxConfig.getPartnerId())) {
      throw new ValidationException("partner-id is invalid [%s]", partnerId);
    }

    RelaxGamingConfiguration.CompanySetting setting = getCompanySettings(partnerId, false);

    Boolean isDemo = mode == "fun";

    String url =
        service.launchUrl(
            isDemo ? RequestContext.instance().withLanguage(language) : 
              RequestContext.instance().withCurrency(demoCurrency).withLanguage(language),
            setting.getLauncherAppClientId(),
            setting.getLauncherAppClientCredential(),
            setting.getLauncherAppApiId(),
            setting.getLauncherAppApiCredential(),
            setting.getLauncherItemApplicationId(),
            Long.parseLong(gameId),
            isDemo,
            token,
            lobbyUrl,
            null);

    try {
      return Response.temporaryRedirect(new URI(url)).build();
    } catch (URISyntaxException e) {
      log.error("Unable to convert url to uri [{}]", url);
      throw new ApplicationException("Unable to convert url to uri");
    }
  }

  /**
   * get operator's company settings
   *
   * @param partnerId
   * @param validateIp
   * @return
   */
  private RelaxGamingConfiguration.CompanySetting getCompanySettings(
      String partnerId, boolean validateIp) {
    String operatorIdKey = String.format("ext-%s", partnerId);
    if (!relaxConfig.getOperatorIdMap().containsKey(operatorIdKey)) {
      throw new ValidationException("no configuration found for operator-id [%s]", partnerId);
    }

    Long companyId = relaxConfig.getOperatorIdMap().get(operatorIdKey);

    if (!relaxConfig.getCompanySettings().containsKey(companyId)) {
      throw new ValidationException("no configuration found for company-id [%s]", companyId);
    }

    if (validateIp) {
      connectorLocator
          .getConnector(OPERATOR_CODE)
          .validateIp(companyId, CommonUtils.resolveIpAddress(this.request));
    }

    return relaxConfig.getCompanySettings().get(companyId);
  }

  /**
   * get locale for i18n.
   *
   * @param language
   * @return
   */
  private Locale getLocale(String language) {
    if (CommonUtils.isEmptyOrNull(language)) {
      return Locale.ENGLISH;
    }

    try {
      return new Locale(language);
    } catch (Exception e) {
      log.debug("Unable to resolve language, return default 'en'");
      return Locale.ENGLISH;
    }
  }

  /**
   * validate request
   * 
   * @param credentials
   * @return
   */
  private boolean authenticate(String auth, Integer partnerId) {
    if (getCompanySettings(String.valueOf(partnerId), false).getOperatorCredential() != auth) {
//      throw new ValidationException("Basic authentication failed. Invalid credentials.")
      log.error("Basic authentication failed. Invalid credentials.");
      return false;
    }
    return true;
  }
}
