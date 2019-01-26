package org.mtransit.parser.ca_red_deer_transit_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MInboundType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// http://data.reddeer.ca/
// http://webmap.reddeer.ca/transit/google_transit.zip
public class RedDeerTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-red-deer-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new RedDeerTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Red Deer Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Red Deer Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		return super.excludeRoute(gRoute);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	private static final String A = "A";

	private static final long ROUTE_ID_ENDS_WITH_A = 10_000L;

	@Override
	public long getRouteId(GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
		}
		Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
		if (matcher.find()) {
			long id = Long.parseLong(matcher.group());
			if (gRoute.getRouteShortName().endsWith(A)) {
				return ROUTE_ID_ENDS_WITH_A + id;
			}
		}
		System.out.printf("\nUnexpected route ID for %s!\n", gRoute);
		System.exit(-1);
		return -1l;
	}

	private static final String YELLOW_SHOOL_BUS_COLOR = "FFD800";

	@Override
	public String getRouteColor(GRoute gRoute) {
		String routeColor = gRoute.getRouteColor();
		if (WHITE.equalsIgnoreCase(routeColor)) {
			routeColor = null;
		}
		if (StringUtils.isEmpty(routeColor)) {
			if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
				int rsn = Integer.parseInt(gRoute.getRouteShortName());
				switch (rsn) {
				// @formatter:off
				case 1: return "B60000";
				case 2: return "32BEBB";
				case 3: return "B48ACA";
				case 4: return "FE0002";
				case 5: return "793E95";
				case 6: return "000000";
				case 7: return "0000B6";
				case 8: return "2DA9AA";
				case 9: return "8E8E8E";
				case 10: return "E95393";
				case 11: return "FFB61A";
				case 12: return "217E7D";
				case 20: return YELLOW_SHOOL_BUS_COLOR;
				case 21: return YELLOW_SHOOL_BUS_COLOR;
				case 22: return YELLOW_SHOOL_BUS_COLOR;
				case 23: return YELLOW_SHOOL_BUS_COLOR;
				case 24: return YELLOW_SHOOL_BUS_COLOR;
				case 25: return YELLOW_SHOOL_BUS_COLOR;
				case 26: return YELLOW_SHOOL_BUS_COLOR;
				case 27: return YELLOW_SHOOL_BUS_COLOR;
				case 28: return YELLOW_SHOOL_BUS_COLOR;
				case 29: return YELLOW_SHOOL_BUS_COLOR;
				case 30: return YELLOW_SHOOL_BUS_COLOR;
				case 31: return YELLOW_SHOOL_BUS_COLOR;
				case 32: return YELLOW_SHOOL_BUS_COLOR;
				case 33: return YELLOW_SHOOL_BUS_COLOR;
				case 34: return YELLOW_SHOOL_BUS_COLOR;
				case 35: return YELLOW_SHOOL_BUS_COLOR;
				case 36: return YELLOW_SHOOL_BUS_COLOR;
				case 37: return YELLOW_SHOOL_BUS_COLOR;
				case 38: return YELLOW_SHOOL_BUS_COLOR;
				case 39: return YELLOW_SHOOL_BUS_COLOR;
				case 40: return YELLOW_SHOOL_BUS_COLOR;
				case 41: return YELLOW_SHOOL_BUS_COLOR;
				case 50: return "000000";
				case 51: return "5DCDF3";
				case 52: return "01B601";
				case 53: return "FE0000";
				case 54: return "6A3683";
				case 100: return "016E01";
				case 101: return "0000B6";
				// @formatter:on
				}
			}
			if ("12A".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "AE7B10";
			}
			if ("35A".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return YELLOW_SHOOL_BUS_COLOR;
			}
			System.out.printf("\nUnexpected route color '%s'\n", gRoute);
			System.exit(-1);
			return null;
		}
		return routeColor;
	}

	private static final Pattern ROUTE_RSN = Pattern.compile("(route [\\d]+[a-zA-Z]?)", Pattern.CASE_INSENSITIVE);

	private static final String _SLASH_ = " / ";

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = cleanRouteLongName(routeLongName);
		if (StringUtils.isEmpty(routeLongName)) {
			if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
				int rsn = Integer.parseInt(gRoute.getRouteShortName());
				switch (rsn) {
				// @formatter:off
				case 1: return "South Hl" + _SLASH_ + "Inglewood";
				case 2: return "Oriole Pk" + _SLASH_ + "Johnstone Pk South";
				case 3: return "College" + _SLASH_ + "Anders Pk";
				case 4: return "Glendale" + _SLASH_ + "Kentwood (West)";
				case 5: return "Rosedale South" + _SLASH_ + "Deer Pk";
				case 6: return "Clearview Rdg" + _SLASH_ + "Timberlands";
				case 7: return "Morrisroe" + _SLASH_ + "Vanier Woods";
				case 8: return "Pines" + _SLASH_ + "Normandeau";
				case 9: return "Eastview" + _SLASH_ + "Inglewood";
				case 10: return "West Pk" + _SLASH_ + "Gaetz Ave South";
				case 11: return "Johnstone Pk (North)" + _SLASH_ + "GH Dawe";
				case 12: return "Gasoline Alley";
				case 20: return "Lindsay Thurber" + _SLASH_ + "Oriole Pk";
				case 21: return "Lindsay Thurber" + _SLASH_ + "Normandeau" + _SLASH_ + "Glendale";
				case 22: return "Lindsay Thurber" + _SLASH_ + "Normandeau";
				case 23: return "Lindsay Thurber" + _SLASH_ + "Eastview Ests ";
				case 24: return "Lindsay Thurber" + _SLASH_ + "East Hl";
				case 25: return "Lindsay Thurber" + _SLASH_ + "Johnstone Pk";
				case 26: return "Hunting Hls" + _SLASH_ + "West Pk";
				case 27: return "Hunting Hls" + _SLASH_ + "Eastview Ests";
				case 28: return "Eastview Middle School" + _SLASH_ + "Eastview Ests";
				case 29: return "Notre Dame" + _SLASH_ + "Hunting Hls" + _SLASH_ + "City Ctr Sorensen Sta";
				case 30: return "City Ctr" + _SLASH_ + "Sorensen Sta";
				case 31: return "Saint Joseph School" + _SLASH_ + "City Ctr" + _SLASH_ + "Sorensen Sta";
				case 32: return "Central Middle School" + _SLASH_ + "Normandeau" + _SLASH_ + "Timberlands";
				case 33: return "Lindsay Thurber" + _SLASH_ + "City Ctr" + _SLASH_ + "Sorensen Sta";
				case 34: return "Saint Joseph School" + _SLASH_ + "Normandeau" + _SLASH_ + "Highland Grn";
				case 35: return "Central Middle School" + _SLASH_ + "Fairview" + _SLASH_ + "Riverside Mdws";
				case 36: return "City Ctr" + _SLASH_ + "Lazy Bus";
				case 37: return "Saint Joseph School" + _SLASH_ + "Timberlands" + _SLASH_ + "Rosedale";
				case 38: return "Clearview" + _SLASH_ + "Deer" + _SLASH_ + "Timberlands" + _SLASH_ + "Rosedale";
				case 39: return "Hunting Hls" + _SLASH_ + "Eastview School" + _SLASH_ + "Morrisroe" + _SLASH_ + "Lancaster";
				case 40: return "Saint Joseph School" + _SLASH_ + "Johnstone Pk" + _SLASH_ + "Kentwood";
				case 41: return "Saint Joseph School" + _SLASH_ + "Kentwood" + _SLASH_ + "Glendale";
				case 50: return "Edgar Ind Pk";
				case 51: return "Gaetz Ave North" + _SLASH_ + "Riverside Ind";
				case 52: return "Riverside Ind Pk" + _SLASH_ + "Olymel";
				case 53: return "Riverside Ind Pk" + _SLASH_ + "Olymel";
				case 54: return "Riverside Ind Pk" + _SLASH_ + "Olymel";
				case 100: return "Lacombe Blackfalds Express";
				case 101: return "Lacombe Blackfalds Local";
				// @formatter:on
				}
			}
			if ("12A".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Gasoline Alley" + _SLASH_ + "Springbrook";
			}
			if ("35A".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Central Middle School" + _SLASH_ + "Oriole Pk";
			}
			System.out.printf("\nUnexpected route long name '%s'\n", gRoute);
			System.exit(-1);
			return null;
		}
		return routeLongName;
	}

	private String cleanRouteLongName(String routeLongName) {
		routeLongName = ROUTE_RSN.matcher(routeLongName).replaceAll(StringUtils.EMPTY);
		routeLongName = INDUSTRIAL.matcher(routeLongName).replaceAll(INDUSTRIAL_REPLACEMENT);
		routeLongName = CleanUtils.removePoints(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return routeLongName;
	}

	private static final String AGENCY_COLOR_RED = "BF311A"; // RED (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_RED;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String INCLUDE_ROUTE_ID = null; // DEBUG

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(1L, new RouteTripSpec(1L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Inglewood", // South Hl
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("935"), // EB VANIER DR @ 30 AV
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("934"), // WB IRONSTONE DR @ 30 AV
								Stops.ALL_STOPS.get("962"), // NB 49 AV @ 33 ST
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(2L, new RouteTripSpec(2L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Johnstone Pk", // Oriole Pk
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("788"), // EB JEWELL ST @ TAYLOR DR
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("786"), // WB JEWELL ST @ TAYLOR DR
								Stops.ALL_STOPS.get("646"), // NB KERRY WOOD DR @ FERN RD
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(3L, new RouteTripSpec(3L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Anders Pk", //
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("1096"), // EB AVERY ST @ AMER CL
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1097"), // WB AVERY ST @ 30 AV
								Stops.ALL_STOPS.get("734"), // NB 54 AV @ 45 ST
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(4L, new RouteTripSpec(4L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Kentwood", // Glendale
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("795"), // WB JORDAN PKY @ TAYLOR DR
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("794"), // EB JORDAN PARKWAY @ STN 5
								Stops.ALL_STOPS.get("1058"), // SB GAETZ AV @ VILLAGE MALL
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(5L, new RouteTripSpec(5L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Deer Pk", // Rosedale South
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("1003"), // != EB 49 ST @ 48 AV
								Stops.ALL_STOPS.get("1005"), // == NB 47 AV @ 51 ST
								Stops.ALL_STOPS.get("1362"), // == != EB 50 ST @30 AV
								Stops.ALL_STOPS.get("1363"), // != <> NB RUTHERFORD DR @ RUTHERFORD CL
								Stops.ALL_STOPS.get("1365"), // != != EB ROLAND ST @ ROBERTS CR
								Stops.ALL_STOPS.get("1364"), // != WB ROLAND ST @ ROBERTS CR
								Stops.ALL_STOPS.get("1366"), // != WB ROLAND ST @ ROBERTS CR
								Stops.ALL_STOPS.get("1173"), // == SB RIDEOUT AV @REICHLEY ST
								Stops.ALL_STOPS.get("1326"), // == SB DAINES @ DUSTON ST
								Stops.ALL_STOPS.get("1211"), // != SB LAWFORD AV @ 32 ST
								Stops.ALL_STOPS.get("1133"), // != NB LOCKWOOD AV @ LANCASTER DR =>
								Stops.ALL_STOPS.get("1155"), // != WB 32 ST @ DAINES AV
								Stops.ALL_STOPS.get("1097"), // != WB AVERY ST @ 30 AV =>
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1096"), // EB AVERY ST @ AMER CL
								Stops.ALL_STOPS.get("1365"), // != EB ROLAND ST @ ROBERTS CR
								Stops.ALL_STOPS.get("1363"), // <> NB RUTHERFORD DR @ RUTHERFORD CL
								Stops.ALL_STOPS.get("1121"), // != WB 50 ST @ 30 AV
								Stops.ALL_STOPS.get("1227"), // WB 55 ST @ 42A AV
								Stops.ALL_STOPS.get("1006"), // SB 47 AV @ 55 ST
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(6L, new RouteTripSpec(6L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Clearview Rdg / Timberlands", //
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("1378"), // EB TIMBERLANDS DR @ 30 AV
								Stops.ALL_STOPS.get("1243"), // EB TIMOTHY DR @ TOBIN GT
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1243"), // EB TIMOTHY DR @ TOBIN GT
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(7L, new RouteTripSpec(7L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Vanier Woods", // Morrisroe /
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("934"), // WB IRONSTONE DR @ 30 AV
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("935"), // EB VANIER DR @ 30 AV
								Stops.ALL_STOPS.get("1133"), // NB LOCKWOOD AV @ LANCASTER DR
								Stops.ALL_STOPS.get("1020"), // WB 35 ST @ 43 AV
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(8L, new RouteTripSpec(8L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Normandeau", // Pines
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("786"), // WB JEWELL ST @ TAYLOR DR
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("788"), // EB JEWELL ST @ TAYLOR DR
								Stops.ALL_STOPS.get("1058"), // SB GAETZ AV @ VILLAGE MALL
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(9L, new RouteTripSpec(9L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Inglewood", // Eastview
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("1134"), // SB LOCKWOOD AV @ 32 ST
								Stops.ALL_STOPS.get("912"), // WB IRONSIDE ST @ INGLIS CR
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("911"), // EB IRONSIDE ST @ 40 AV
								Stops.ALL_STOPS.get("1046"), // WB 44 ST @ 40 AV
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(10L, new RouteTripSpec(10L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Inglewood", // West Pk / Gaetz Ave South
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("911"), // EB IRONSIDE ST @ 40 AV
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("912"), // WB IRONSIDE ST @ INGLIS CR
								Stops.ALL_STOPS.get("897"), // NB 50 AV @ BENNETT
								Stops.ALL_STOPS.get("733"), // EB 43 ST @ TAYLOR DR
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(11L, new RouteTripSpec(11L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Johnstone Pk", // GH Dawe
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta") //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("794"), // EB JORDAN PARKWAY @ STN 5
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("795"), // WB JORDAN PKY @ TAYLOR DR
								Stops.ALL_STOPS.get("760"), // EB HORN ST @ TAYLOR DR
								Stops.ALL_STOPS.get("1058"), // SB GAETZ AV @ VILLAGE MALL
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(12L, new RouteTripSpec(12L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("878"), // Twp Rd 273a @ Petrolia Dr
								Stops.ALL_STOPS.get("635"), // ++
								Stops.ALL_STOPS.get("900"), // WB BENNETT ST @ BAKER AV
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("900"), // WB BENNETT ST @ BAKER AV
								Stops.ALL_STOPS.get("886"), // ++
								Stops.ALL_STOPS.get("878"), // Twp Rd 273a @ Petrolia Dr
						})) //
				.compileBothTripSort());
		map2.put(12L + ROUTE_ID_ENDS_WITH_A, new RouteTripSpec(12L + ROUTE_ID_ENDS_WITH_A, // 12A
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("623"), // Airport Dr @ Tamarac Bl
								Stops.ALL_STOPS.get("950"), // ++
								Stops.ALL_STOPS.get("904"), // WB BENNETT ST @ BARRETT DR
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("900"), // WB BENNETT ST @ BAKER AV
								Stops.ALL_STOPS.get("886"), // ++
								Stops.ALL_STOPS.get("878"), // ++ Twp Rd 273a @ Petrolia Dr
								Stops.ALL_STOPS.get("635"), // ++
								Stops.ALL_STOPS.get("623"), // Airport Dr @ Tamarac Bl
						})) //
				.compileBothTripSort());
		map2.put(50L, new RouteTripSpec(50L, //
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta", //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Edgar Ind") //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1226"), // SB TAYLOR AV @ EDGAR IND DR
								Stops.ALL_STOPS.get("763"), // ++ SB TAYLOR DR @ 68 ST
								Stops.ALL_STOPS.get("1267"), // Sorensen Station 49 AV @ 48 ST
						})) //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // Sorensen Station 49 AV @ 48 ST
								Stops.ALL_STOPS.get("988"), // NB 49 AV @ 49 ST
								Stops.ALL_STOPS.get("754"), // ++ NB JOHNSTONE DR @ 67 AV
								Stops.ALL_STOPS.get("1226"), // SB TAYLOR AV @ EDGAR IND DR
						})) //
				.compileBothTripSort());
		map2.put(51L, new RouteTripSpec(51L, //
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr" + _SLASH_ + "Sorensen Sta", //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Riverside Ind") // Gaetz Av North
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1083"), // EB 77 ST @ 40 AV
								Stops.ALL_STOPS.get("1082"), // SB RIVERSIDE DR @ 77 ST
								Stops.ALL_STOPS.get("1069"), // ++
								Stops.ALL_STOPS.get("988"), // NB 49 AV @ 49 ST #DOWNTOWN
						})) //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // Sorensen Station 49 AV @ 48 ST #DOWNTOWN
								Stops.ALL_STOPS.get("993"), // ++
								Stops.ALL_STOPS.get("1083"), // EB 77 ST @ 40 AV
						})) //
				.compileBothTripSort());
		map2.put(52L, new RouteTripSpec(52L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Riverside Dr", // "Riverside Ind" // Olymel
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, StringUtils.EMPTY) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("701"), // SB 57 AV @ 41 ST
								Stops.ALL_STOPS.get("997"), // WB RIVERSIDE DR @ 48 AV
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						/* no stops */
						})) //
				.compileBothTripSort());
		map2.put(53L, new RouteTripSpec(53L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Riverside Dr", // "Riverside Ind Pk" // Olymel
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, StringUtils.EMPTY) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("798"), // WB HORN ST @ 61 AV
								Stops.ALL_STOPS.get("997"), // WB RIVERSIDE DR @ 48 AV
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						/* no stops */
						})) //
				.compileBothTripSort());
		map2.put(54L, new RouteTripSpec(54L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Riverside Dr", // "Riverside Ind Pk" // Olymel
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, StringUtils.EMPTY) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("911"), // EB IRONSIDE ST @ 40 AV
								Stops.ALL_STOPS.get("1081"), // NB RIVERSIDE DR @ 76 ST
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						/* no stops */
						})) //
				.compileBothTripSort());
		map2.put(100L, new RouteTripSpec(100L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Lacombe", // Blackfalds Express
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Red Deer") // Sorensen Sta
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("1077"), // NB GAETZ AV @ 78 ST
								Stops.ALL_STOPS.get("1303"), // WB COLLEGE AVE @ 52 ST
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1303"), // WB COLLEGE AVE @ 52 ST
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		map2.put(101L, new RouteTripSpec(101L, //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Lacombe", // Blackfalds Express
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Red Deer") // Sorensen Sta
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
								Stops.ALL_STOPS.get("1303"), // WB COLLEGE AVE @ 52 ST
						})) //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { //
						Stops.ALL_STOPS.get("1303"), // WB COLLEGE AVE @ 52 ST
								Stops.ALL_STOPS.get("1267"), // 49 AV @ 48 ST SORENSEN STN
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		String tripHeadsign = gTrip.getTripHeadsign();
		if (gTrip.getRouteId().endsWith("-IB")) {
			mTrip.setHeadsignInbound(MInboundType.INBOUND);
			return;
		} else if (gTrip.getRouteId().endsWith("-OB")) {
			mTrip.setHeadsignInbound(MInboundType.OUTBOUND);
			return;
		}
		int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
		mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), directionId);
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("^[\\d]+[a-zA-Z]? \\- ", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		if (Utils.isUppercaseOnly(tripHeadsign, true, true)) {
			tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		}
		tripHeadsign = STARTS_WITH_RSN.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern INDUSTRIAL = Pattern.compile("((^|\\W){1}(industrial)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String INDUSTRIAL_REPLACEMENT = "$2" + "Ind" + "$4";

	private static final Pattern BOUNDS = Pattern.compile("((^|\\W){1}(sb|nb|eb|wb)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		gStopName = BOUNDS.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = INDUSTRIAL.matcher(gStopName).replaceAll(INDUSTRIAL_REPLACEMENT);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(GStop gStop) {
		return Integer.parseInt(gStop.getStopCode()); // use stop code as stop ID
	}
}
