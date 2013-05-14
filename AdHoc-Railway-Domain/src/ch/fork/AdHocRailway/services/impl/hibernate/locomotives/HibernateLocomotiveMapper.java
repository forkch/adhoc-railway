package ch.fork.AdHocRailway.services.impl.hibernate.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;

public class HibernateLocomotiveMapper {

	public static Locomotive mapLocomotive(
			final HibernateLocomotive hibernateLocomotive) {

		final Locomotive locomotive = new Locomotive();
		updateLocomotive(hibernateLocomotive, locomotive);
		return locomotive;
	}

	private static LocomotiveType mapType(final String locomotiveType) {
		if (locomotiveType.equalsIgnoreCase("DELTA")) {
			return LocomotiveType.DELTA;
		} else if (locomotiveType.equalsIgnoreCase("DIGITAL")) {
			return LocomotiveType.DIGITAL;
		}
		return null;
	}

	private static String mapType(final LocomotiveType locomotiveType) {
		return locomotiveType.toString();
	}

	public static LocomotiveGroup mapHibernateLocomotiveGroup(
			final HibernateLocomotiveGroup hLocomotiveGroup) {
		final LocomotiveGroup group = new LocomotiveGroup(
				hLocomotiveGroup.getId(), hLocomotiveGroup.getName());
		for (final HibernateLocomotive hLocomotive : hLocomotiveGroup
				.getLocomotives()) {
			final Locomotive locomotive = mapLocomotive(hLocomotive);
			group.addLocomotive(locomotive);
			locomotive.setGroup(group);
		}
		return group;
	}

	public static HibernateLocomotive mapLocomotive(final Locomotive locomotive) {
		final HibernateLocomotive hLocomotive = new HibernateLocomotive();
		updateHibernateLocomotive(hLocomotive, locomotive);
		return hLocomotive;
	}

	public static HibernateLocomotiveGroup mapLocomotiveGroup(
			final LocomotiveGroup locomotiveGroup) {
		final HibernateLocomotiveGroup hLocomotiveGroup = new HibernateLocomotiveGroup();
		updateHibernateLocomotiveGroup(hLocomotiveGroup, locomotiveGroup);
		return hLocomotiveGroup;
	}

	public static void updateHibernateLocomotiveGroup(
			final HibernateLocomotiveGroup hLocomotiveGroup,
			final LocomotiveGroup locomotiveGroup) {
		hLocomotiveGroup.setId(locomotiveGroup.getId());
		hLocomotiveGroup.setName(locomotiveGroup.getName());
	}

	public static void updateLocomotive(
			final HibernateLocomotive hibernateLocomotive,
			final Locomotive locomotive) {
		locomotive.setName(hibernateLocomotive.getName());
		locomotive.setId(hibernateLocomotive.getId());
		locomotive.setBus(hibernateLocomotive.getBus());
		locomotive.setDesc(hibernateLocomotive.getDescription());
		locomotive.setImage(hibernateLocomotive.getImage());
		locomotive.setAddress1(hibernateLocomotive.getAddress());
		locomotive.setType(mapType(hibernateLocomotive.getType()));
	}

	public static void updateHibernateLocomotive(
			final HibernateLocomotive hLocomotive, final Locomotive locomotive) {
		hLocomotive.setName(locomotive.getName());
		hLocomotive.setId(locomotive.getId());
		hLocomotive.setAddress(locomotive.getAddress1());
		hLocomotive.setBus(locomotive.getBus());
		hLocomotive.setDescription(locomotive.getDesc());
		hLocomotive.setImage(locomotive.getImage());
		hLocomotive.setType(mapType(locomotive.getType()));
	}
}
