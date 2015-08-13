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
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	private static final String A = "A";

	private static final long ROUTE_ID_ENDS_WITH_A = 10000l;

	@Override
	public long getRouteId(GRoute gRoute) {
		if (gRoute.getRouteShortName() != null && gRoute.getRouteShortName().length() > 0 && Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
		}
		Matcher matcher = DIGITS.matcher(gRoute.getRouteId());
		matcher.find();
		long id = Long.parseLong(matcher.group());
		if (gRoute.getRouteShortName().endsWith(A)) {
			return ROUTE_ID_ENDS_WITH_A + id;
		}
		System.out.printf("\nUnexpected route ID for %s!\n", gRoute);
		System.exit(-1);
		return -1l;
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
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
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(50l, new RouteTripSpec(50l, //
				MInboundType.INBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "City Ctr Term", //
				MInboundType.OUTBOUND.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Edgar Ind") //
				.addTripSort(MInboundType.INBOUND.intValue(), //
						Arrays.asList(new String[] { "1226", "763", "1267" })) //
				.addTripSort(MInboundType.OUTBOUND.intValue(), //
						Arrays.asList(new String[] { "1267", "754", "1226" })) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.id)) {
			return ALL_ROUTE_TRIPS2.get(mRoute.id).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.id)) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.id));
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.id)) {
			return; // split
		}
		if (mRoute.id == 6l) {
			if (gTrip.getRouteId().endsWith("-IB")) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else if (gTrip.getRouteId().endsWith("-OB")) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		}
		if (gTrip.getRouteId().endsWith("-IB")) {
			mTrip.setHeadsignInbound(MInboundType.INBOUND);
			return;
		} else if (gTrip.getRouteId().endsWith("-OB")) {
			mTrip.setHeadsignInbound(MInboundType.OUTBOUND);
			return;
		}
		String tripHeadsign = gTrip.getTripHeadsign();
		if (StringUtils.isEmpty(tripHeadsign)) {
			if (mRoute.id == 12l) {
				tripHeadsign = "Loop";
			} else if (mRoute.id == 12l + ROUTE_ID_ENDS_WITH_A) { // 12A
				tripHeadsign = "Loop";
			} else if (mRoute.id == 52l) {
				tripHeadsign = "Riverside Dr";
			} else if (mRoute.id == 53l) {
				tripHeadsign = "Riverside Dr";
			} else if (mRoute.id == 54l) {
				tripHeadsign = "Olymel";
			} else {
				System.out.printf("\n%s: Unexpected trip %s!\n", mRoute.id, gTrip);
				System.exit(-1);
			}
		}
		int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
		mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), directionId);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern INDUSTRIAL = Pattern.compile("((^|\\W){1}(industrial)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String INDUSTRIAL_REPLACEMENT = "$2Ind$4";

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
}
