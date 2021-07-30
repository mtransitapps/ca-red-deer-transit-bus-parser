package org.mtransit.parser.ca_red_deer_transit_bus;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.ColorUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://data.reddeer.ca/gtfsdatafeed
// https://data.reddeer.ca/data/GTFS/RD_GTFS.zip
// OTHER: https://webmap.reddeer.ca/transit/google_transit.zip
public class RedDeerTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new RedDeerTransitBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Red Deer";
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
		String rsn = gRoute.getRouteShortName();
		if (rsn.startsWith("R")) {
			rsn = rsn.substring(1);
		}
		if (CharUtils.isDigitsOnly(rsn)) {
			return Long.parseLong(rsn); // use route short name as route ID
		}
		final Matcher matcher = DIGITS.matcher(rsn);
		if (matcher.find()) {
			final long id = Long.parseLong(matcher.group());
			if (rsn.endsWith(A)) {
				return ROUTE_ID_ENDS_WITH_A + id;
			}
		}
		throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute.toStringPlus());
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
			String rsnS = gRoute.getRouteShortName();
			if (rsnS.startsWith("R")) {
				rsnS = rsnS.substring(1);
			}
			if (CharUtils.isDigitsOnly(rsnS)) {
				int rsn = Integer.parseInt(rsnS);
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
			if ("12A".equalsIgnoreCase(rsnS)) {
				return "AE7B10";
			}
			if ("35A".equalsIgnoreCase(rsnS)) {
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
		String routeLongName = gRoute.getRouteLongNameOrDefault();
		routeLongName = cleanRouteLongName(routeLongName);
		if (StringUtils.isEmpty(routeLongName)) {
			if (CharUtils.isDigitsOnly(gRoute.getRouteShortName())) {
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
		routeLongName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, routeLongName, getIgnoredWords());
		routeLongName = ROUTE_RSN.matcher(routeLongName).replaceAll(EMPTY);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_RED = "BF311A"; // RED (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_RED;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
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
