package org.mtransit.parser.ca_red_deer_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.ColorUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.StringUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// https://data.reddeer.ca/gtfsdatafeed
// https://data.reddeer.ca/data/GTFS/RD_GTFS.zip
// OTHER: https://webmap.reddeer.ca/transit/google_transit.zip
public class RedDeerTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-red-deer-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new RedDeerTransitBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Red Deer Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Red Deer Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	private static final String A = "A";

	private static final long ROUTE_ID_ENDS_WITH_A = 10_000L;

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
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
	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		String routeColor = gRoute.getRouteColor();
		if (ColorUtils.WHITE.equalsIgnoreCase(routeColor)) {
			routeColor = null;
		}
		if (StringUtils.isEmpty(routeColor)) {
			if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
				int rsn = Integer.parseInt(gRoute.getRouteShortName());
				if (rsn >= 10 && rsn < 20) {
					return "5E5F5F"; // GRAY
				}
				if (rsn >= 20 && rsn <= 42) {
					return YELLOW_SCHOOL_BUS_COLOR;
				}
				switch (rsn) {
				// @formatter:off
				case 1: return "E02450"; // PINK
				case 2: return "1BAFA0"; // BLUE
				case 3: return "DCA340"; // YELLOW MUSTARD
				case 4: return "8A5494"; // PURPLE
				case 53: return null; // TODO?
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
			throw new MTLog.Fatal("Unexpected route color '%s'", gRoute.toStringPlus());
		}
		return routeColor;
	}

	private static final Pattern ROUTE_RSN = Pattern.compile("(route [\\d]+[a-z]?)", Pattern.CASE_INSENSITIVE);

	private static final String _SLASH_ = " / ";

	@NotNull
	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
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
				case 20: return "Oriole Pk, Fairview" + _SLASH_ + "Lindsay Thurber Comprehensive HS";
				case 21: return "Glendale, Normandeau, Highland Green" + _SLASH_ + "Lindsay Thurber Comprehensive HS";
				case 22: return "Pines, Kentwood, Normandeau" + _SLASH_ + "Lindsay Thurber Comprehensive HS";
				case 23: return "Clearview, Rosedale, Deer Pk (Morth), Eastview Ests" + _SLASH_ + "Lindsay Thurber Comprehensive HS";
				case 24: return "East Hl - Morrisroe, Sunnybrook, Deer Pk (South), Anders, Lancaster" + _SLASH_ + "Lindsay Thurber Comprehensive HS";
				case 25: return "Kentwood, Johnstone, Oriole Pk West, Riverside Mdws" + _SLASH_ + "Lindsay Thurber Comprehensive HS";
				case 26: return "West Pk, Bower" + _SLASH_ + "Hunting Hls & Notre Dame HS";
				case 27: return "Eastview Ests, Clearview, Rosedale, Deer Pk (North)" + _SLASH_ + "Hunting Hls & Notre Dame HS";
				case 28: return "Eastview Ests, Clearview, Rosedale, Deer Pk (North)" + _SLASH_ + "Eastview Middle School";
				case 29: return "City Ctr" + _SLASH_ + "Hunting Hls & Notre Dame HS";
				case 30: return "Sorensen Sta (Downtown)" + _SLASH_ + "Gateway Christian S";
				case 31: return "Sorensen Sta (Downtown)" + _SLASH_ + "St Joseph HS";
				case 32: return "Timberlands, Clearview Rdg, Garden Hghts, Pines, Normandeau" + _SLASH_ + "Central Middle School";
				case 33: return "Sorensen Sta (Downtown)" + _SLASH_ + "Lindsay Thurber Comprehensive HS";
				case 34: return "Highland Green, Normandeau, Pines" + _SLASH_ + "St Joseph HS";
				case 35: return "Oriole Pk West, Oriole Pk, Riverside Mdws, Fairview" + _SLASH_ + "Central Middle School";
				case 36: return "Sorensen Sta (Downtown)" + _SLASH_ + "Central Middle School";
				case 37: return "Clearview, Timberlands, Rosedale" + _SLASH_ + "St Joseph HS";
				case 38: return "Eastview, Deer Pk, Clearview" + _SLASH_ + "St Joseph HS";
				case 39: return "Anders-on-the-Lk, Lancaster, Deer Pk (South), Morrisroe" + _SLASH_ + "Eastview Middle School";
				case 40: return "Johnstone Pk, Johnstone Crossing, Kentwood" + _SLASH_ + "St Joseph HS";
				case 41: return "Glendale, Kentwood (South), Normandeau" + _SLASH_ + "St Joseph HS";
				case 42: return "Timberlands, Garden Hghts, Clearview North" + _SLASH_ + "Lindsay Thurber Comprehensive HS";
				case 53: return "Riverside Industrial / Olymel Routes";
				case 103: return "2A South Regional";
				case 104: return "2A South Regional";
				// @formatter:on
				}
			}
			if ("35A".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Oriole Pk West, Oriole Pk, Riverside Mdws, Fairview" + _SLASH_ + "Central Middle School";
			}
			throw new MTLog.Fatal("Unexpected route long name '%s'", gRoute.toStringPlus());
		}
		return routeLongName;
	}

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = ROUTE_RSN.matcher(routeLongName).replaceAll(EMPTY);
		//noinspection deprecation
		routeLongName = CleanUtils.removePoints(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return routeLongName;
	}

	private static final String AGENCY_COLOR_RED = "BF311A"; // RED (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_RED;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern STARTS_WITH_BOUNDS_ = Pattern.compile("(^(nb|sb|eb|wb) )", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanDirectionHeadsign(boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = super.cleanDirectionHeadsign(fromStopName, directionHeadSign);
		directionHeadSign = STARTS_WITH_BOUNDS_.matcher(directionHeadSign).replaceAll(EMPTY);
		return directionHeadSign;
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("^[\\d]+[a-z]? - ", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_INBOUND_DASH = Pattern.compile("^Inbound - ", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = STARTS_WITH_RSN.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = STARTS_WITH_INBOUND_DASH.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("Need to merge trip head-signs: '%s' VS '%s'", mTrip, mTripToMerge);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"EB", "WB", "NB", "SW",
				"AM", "PM",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		String stopCode = gStop.getStopCode();
		if (stopCode.startsWith("RD")) {
			stopCode = stopCode.substring(2);
		}
		return Integer.parseInt(stopCode); // use stop code as stop ID
	}
}
