package org.mtransit.parser.ca_red_deer_transit_bus;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.ColorUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://data.reddeer.ca/gtfsdatafeed
// https://data.reddeer.ca/data/GTFS/RD_GTFS.zip
// OTHER: https://webmap.reddeer.ca/transit/google_transit.zip
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
		MTLog.log("Generating Red Deer Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		MTLog.log("Generating Red Deer Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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
		throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute);
	}

	private static final String YELLOW_SCHOOL_BUS_COLOR = "FFD800";

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Override
	public String getRouteColor(GRoute gRoute) {
		String routeColor = gRoute.getRouteColor();
		if (ColorUtils.WHITE.equalsIgnoreCase(routeColor)) {
			routeColor = null;
		}
		if (StringUtils.isEmpty(routeColor)) {
			if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
				int rsn = Integer.parseInt(gRoute.getRouteShortName());
				if (rsn >= 10) {
					return "5E5F5F"; // GRAY
				}
				switch (rsn) {
				// @formatter:off
				case 1: return "E02450"; // PINK
				case 2: return "1BAFA0"; // BLUE
				case 3: return "DCA340"; // YELLOW MUSTARD
				case 4: return "8A5494"; // PURPLE
				case 103: return null; // TODO?
				case 104: return null; // TODO?
				// @formatter:on
				}
			}
			if ("12A".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "AE7B10";
			}
			if ("35A".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return YELLOW_SCHOOL_BUS_COLOR;
			}
			throw new MTLog.Fatal("Unexpected route color '%s'", gRoute);
		}
		return routeColor;
	}

	private static final Pattern ROUTE_RSN = Pattern.compile("(route [\\d]+[a-zA-Z]?)", Pattern.CASE_INSENSITIVE);

	private static final String _SLASH_ = " / ";

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = cleanRouteLongName(routeLongName);
		if (StringUtils.isEmpty(routeLongName)) {
			if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
				int rsn = Integer.parseInt(gRoute.getRouteShortName());
				switch (rsn) {
				// @formatter:off
				case 1: return "Gaetz Ave Rapid Bus";
				case 2: return "Crosstown";
				case 3: return "Hospital" + _SLASH_ + "College";
				case 4: return "Glendale" + _SLASH_ + "Southeast";
				case 10: return "Rosedale";
				case 11: return "Anders" + _SLASH_ + "Vanier Woods";
				case 12: return "Gasoline Alley";
				case 13: return "West Pk" + _SLASH_ + "College" + _SLASH_ + "Bower";
				case 15: return "Pines" + _SLASH_ + "Normandeau";
				case 16: return "Oriole Pk";
				case 18: return "Riverside Ind";
				case 19: return "Edgar Ind";
				case 103: return "2A South Regional";
				case 104: return "2A South Regional";
				// @formatter:on
				}
			}
			throw new MTLog.Fatal("Unexpected route long name '%s'", gRoute);
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

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		map2.put(1L, new RouteTripSpec(1L, // SAME HEAD-SIGNS for both
				0, MTrip.HEADSIGN_TYPE_STRING, "Bower", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Kingston") //
				.addTripSort(0, //
						Arrays.asList( //
								Stops.getALL_STOPS().get("1079"), // EB KINGSTON DR @ GAETZ AV #KINGSTON
								Stops.getALL_STOPS().get("897") // NB 50 AV @ BENNETT #BOWER
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								Stops.getALL_STOPS().get("897"), // NB 50 AV @ BENNETT #BOWER
								Stops.getALL_STOPS().get("1078") // WB KINGSTON DR @ GAETZ AV #KINGSTON
						)) //
				.compileBothTripSort());
		map2.put(2L, new RouteTripSpec(2L, // SAME HEAD-SIGNS for both
				0, MTrip.HEADSIGN_TYPE_STRING, "Bower", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Kingston") //
				.addTripSort(0, //
						Arrays.asList( //
								Stops.getALL_STOPS().get("1078"), // WB KINGSTON DR @ GAETZ AV #KINGSTON
								Stops.getALL_STOPS().get("1060"), // 67 ST @ GAETZ AV
								Stops.getALL_STOPS().get("900") // WB BENNETT ST @ BAKER AV #BOWER
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								Stops.getALL_STOPS().get("900"), // WB BENNETT ST @ BAKER AV #BOWER
								Stops.getALL_STOPS().get("1079") // EB KINGSTON DR @ GAETZ AV #KINGSTON
						)) //
				.compileBothTripSort());
		map2.put(3L, new RouteTripSpec(3L, // SAME HEAD-SIGNS for both
				0, MTrip.HEADSIGN_TYPE_STRING, "Bower", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Sorensen")
				.addTripSort(0, //
						Arrays.asList( //
								Stops.getALL_STOPS().get("1267"), // Sorensen Station 49 AV @ 48 ST #SORENSEN
								Stops.getALL_STOPS().get("900") // WB BENNETT ST @ BAKER AV #BOWER
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								Stops.getALL_STOPS().get("900"), // WB BENNETT ST @ BAKER AV #BOWER
								Stops.getALL_STOPS().get("1267") // Sorensen Station 49 AV @ 48 ST #SORENSEN
						)) //
				.compileBothTripSort());
		map2.put(4L, new RouteTripSpec(4L, // SAME HEAD-SIGNS for both
				0, MTrip.HEADSIGN_TYPE_STRING, "Bower", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Kingston") //
				.addTripSort(0, //
						Arrays.asList( //
								Stops.getALL_STOPS().get("1079"), // EB KINGSTON DR @ GAETZ AV #KINGSTON
								Stops.getALL_STOPS().get("901") // EB BENNETT ST @ BAKER AV #BOWER
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								Stops.getALL_STOPS().get("901"), // EB BENNETT ST @ BAKER AV #BOWER
								Stops.getALL_STOPS().get("1092"), // WB 39 ST @ ELLENWOOD DR
								Stops.getALL_STOPS().get("1079") // EB KINGSTON DR @ GAETZ AV #KINGSTON
						)) //
				.compileBothTripSort());
		map2.put(11L, new RouteTripSpec(11L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Bower", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Sorensen")
				.addTripSort(0, //
						Arrays.asList( //
								Stops.getALL_STOPS().get("1267"), // 49 AV @ 48 ST SORENSEN STN #SORENSEN
								Stops.getALL_STOPS().get("900") // WB BENNETT ST @ BAKER AV #BOWER
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								Stops.getALL_STOPS().get("900"), // WB BENNETT ST @ BAKER AV #BOWER
								Stops.getALL_STOPS().get("1267") // 49 AV @ 48 ST SORENSEN STN #SORENSEN
						)) //
				.compileBothTripSort());
		map2.put(13L, new RouteTripSpec(13L, //
			0, MTrip.HEADSIGN_TYPE_STRING, "Bower", //
			1, MTrip.HEADSIGN_TYPE_STRING, "Sorensen")
			.addTripSort(0, //
					Arrays.asList( //
							Stops.getALL_STOPS().get("1267"), // 49 AV @ 48 ST SORENSEN STN #SORENSEN
							Stops.getALL_STOPS().get("900") // WB BENNETT ST @ BAKER AV #BOWER
					)) //
			.addTripSort(1, //
					Arrays.asList( //
							Stops.getALL_STOPS().get("900"), // WB BENNETT ST @ BAKER AV #BOWER
							Stops.getALL_STOPS().get("1267") // 49 AV @ 48 ST SORENSEN STN #SORENSEN
					)) //
			.compileBothTripSort());
		map2.put(103L, new RouteTripSpec(103L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								Stops.getALL_STOPS().get("1402"), // 42ND ST @ 51AV
								Stops.getALL_STOPS().get("900") // WB BENNETT ST @ BAKER AV
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								Stops.getALL_STOPS().get("900"), // WB BENNETT ST @ BAKER AV
								Stops.getALL_STOPS().get("1402") // 42ND ST @ 51AV
						)) //
				.compileBothTripSort());
		map2.put(104L, new RouteTripSpec(104L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								Stops.getALL_STOPS().get("1402"), // 42ND ST @ 51AV
								Stops.getALL_STOPS().get("900") // WB BENNETT ST @ BAKER AV
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								Stops.getALL_STOPS().get("900"), // WB BENNETT ST @ BAKER AV
								Stops.getALL_STOPS().get("1402") // 42ND ST @ 51AV
						)) //
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
		int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
		mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), directionId);
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("^[\\d]+[a-zA-Z]? - ", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_INBOUND_DASH = Pattern.compile("^Inbound - ", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		if (Utils.isUppercaseOnly(tripHeadsign, true, true)) {
			tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		}
		tripHeadsign = STARTS_WITH_RSN.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_INBOUND_DASH.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		throw new MTLog.Fatal("Need to merge trip head-signs: '%s' VS '%s'", mTrip, mTripToMerge);
	}

	private static final Pattern INDUSTRIAL = Pattern.compile("((^|\\W)(industrial)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String INDUSTRIAL_REPLACEMENT = "$2" + "Ind" + "$4";

	private static final Pattern BOUNDS = Pattern.compile("((^|\\W)(sb|nb|eb|wb)(\\W|$))", Pattern.CASE_INSENSITIVE);

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
